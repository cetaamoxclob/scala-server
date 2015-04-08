package compiler

import com.tantalim.models._
import com.tantalim.util.TantalimException
import compiler.src.{FieldDefaultJson, ModelFieldJson, ModelJson}
import play.api.libs.json.{JsError, JsSuccess}
import services.ArtifactService

import scala.collection.{Seq, Map}

trait ModelCompiler extends ArtifactService with TableCompiler {

  def compileModel(name: String): Model = {
    println("Compiling model set " + name)
    val json = getArtifactContentAndParseJson(ArtifactType.Model, name)
    json.validate[ModelJson] match {
      case JsSuccess(modelJson, _) =>
        compileModelView(modelJson.copy(name = Option(name)), None)
      case JsError(err) =>
        throw new TantalimException("Failed to compile model " + name, "See the following error:" + err.toString)
    }
  }

  def compileModel(table: DeepTable): Model = {
    println("Creating model with table " + table.name)
    def compileField(column: TableColumn): ModelField = {
      new ModelField(
        column.name,
        column,
        updateable = column.updateable,
        required = column.required
      )
    }

    val TableOnly = "TableOnly"
    val model = new Model(
      table.name + TableOnly,
      table,
      fields = table.columns.map { case (columnName, column) =>
        columnName -> compileField(column)
      },
      instanceID = if (table.primaryKey.isDefined) Some(compileField(table.primaryKey.get)) else None,
      allowInsert = table.allowInsert,
      allowUpdate = table.allowUpdate,
      allowDelete = table.allowDelete
    )

    model
  }

  private def extendModel(model: ModelJson, parent: Option[Model]): Model = {
    println("Extending model " + model.extendModel.get)
    val superModel = parent.get
    val modelFields = superModel.fields ++
      convertJsonFieldsToModelFieldMap(
        model.fields,
        superModel.basisTable,
        buildModelSteps(model, superModel.basisTable)
      )

    superModel.copy(
      name = model.name.get,
      parentField = model.parentField,
      childField = model.childField,
      fields = modelFields,
      parent = parent,
      filter = if (model.filter.isDefined) model.filter else superModel.filter,
      preSave = model.preSave
    )
  }

  private def compileModelView(model: ModelJson, parent: Option[Model]): Model = {
    if (model.name.isEmpty) {
      throw new TantalimException("Model Name is missing", "Add name to " + model)
    }
    println("Compiling model " + model.name.get)
    val basisTable = getBasisTable(model)
    val steps = buildModelSteps(model, basisTable)
    val modelFields = convertJsonFieldsToModelFieldMap(model.fields, basisTable, steps)

    val newModel = createModel(model, basisTable, modelFields, parent, steps)
    if (newModel.parent.isDefined && !newModel.basisTable.isMock) checkChildModel(newModel)
    if (model.children.isDefined) {
      model.children.get.foreach { childJson =>
        addChildModelToParentModel(childJson, newModel)
      }
    }
    newModel
  }

  private def createModel(model: ModelJson, basisTable: DeepTable, modelFields: Map[String, ModelField], parent: Option[Model], steps: Seq[ModelStep]): Model = {
    println("Creating new Model " + model.name.get)
    new Model(
      model.name.get,
      basisTable,
      model.limit.getOrElse(0),
      fields = modelFields.toMap,
      instanceID = findInstanceIdField(basisTable.primaryKey, modelFields.values),
      parentField = model.parentField,
      childField = model.childField,
      parent = parent,
      steps = convertStepsToMap(steps),
      orderBy = compileOrderBy(model.orderBy),
      allowInsert = model.allowInsert.getOrElse(basisTable.allowInsert),
      allowUpdate = model.allowUpdate.getOrElse(basisTable.allowUpdate),
      allowDelete = model.allowDelete.getOrElse(basisTable.allowDelete),
      preSave = model.preSave,
      filter = model.filter,
      customUrlSource = model.customUrlSource
    )
  }

  private def findInstanceIdField(primaryKey: Option[TableColumn], modelFields: Iterable[ModelField]): Option[ModelField] = {
    if (primaryKey.isDefined) {
      modelFields.find(f =>
        f.basisColumn.name == primaryKey.get.name && f.step.isEmpty
      )
    } else None
  }

  private def convertJsonFieldsToModelFieldMap(modelFields: Option[Seq[ModelFieldJson]], basisTable: Table, steps: Seq[ModelStep]): Map[String, ModelField] = {
    if (modelFields.isEmpty) Map.empty
    else modelFields.get.map(f => {
      if (f.step.isDefined) {
        val fieldStep = findStepByName(f.step.get, steps)
        val tableJoin = fieldStep.join

        f.name -> compileModelField(f,
          tableJoin.table.getColumn(f.basisColumn),
          Some(fieldStep)
        )
      } else {
        f.name -> compileModelField(f, basisTable.getColumn(f.basisColumn), None)
      }
    }).toMap
  }

  private def getBasisTable(model: ModelJson): DeepTable = {
    if (Table.isMock(model.basisTable)) {
      Table.createMock
    } else {
      getTableFromCache(model.basisTable).getOrElse {
        val newTable = compileTable(model.basisTable)
        addTableToCache(model.name.get, newTable)
        newTable
      }
    }
  }

  private def checkChildModel(childModel: Model): Unit = {
    if (childModel.parentField.isEmpty) {
      // We could try to guess based on the parent model's identifier field ???
      throw new TantalimException(s"Model named ${childModel.name} is missing parentField", "")
    }
    if (childModel.childField.isEmpty) {
      throw new TantalimException(s"Model named ${childModel.name} is missing childField", "")
    }
    val childField = childModel.getField(childModel.childField.get)
    val parentField = childModel.parent.get.getField(childModel.parentField.get)
    if (childField.dataType != parentField.dataType) {
      throw new TantalimException(s"parent and child fields are not of the same data type in ${childModel.name}",
        s"${childField.name} is a ${childField.dataType} and ${parentField.name} is a ${parentField.dataType}")
    }
  }

  private def addChildModelToParentModel(childJson: ModelJson, parentModel: Model): Unit = {
    val childModel =
      if (childJson.extendModel.isDefined) extendModel(childJson, Some(parentModel))
      else compileModelView(childJson, Some(parentModel))
    parentModel.children(childModel.name) = childModel

  }

  private def findStepByName(stepName: String, steps: Seq[ModelStep]): ModelStep = {
    steps.find(step => step.name == stepName).getOrElse(
      throw new TantalimException(s"Can't find step `$stepName`", s"Existing steps are: ${steps.mkString(", ")}")
    )
  }

  private def convertStepsToMap(steps: Seq[ModelStep]): Map[Int, ModelStep] = {
    steps.map { step =>
      step.tableAlias -> step
    }.toMap
  }

  private def buildModelSteps(model: ModelJson, basisTable: DeepTable): Seq[ModelStep] = {
    buildTempModelSteps(model, basisTable).map { step =>
      val join = step.tableJoin.getOrElse(throw new TantalimException(s"tableJoin for $step is still missing", "Add or rename the step"))
      new ModelStep(
        name = step.name,
        tableAlias = step.tableAlias.get,
        join = join,
        required = step.required.get,
        parentAlias = step.parent match {
          case Some(p) => p.tableAlias.get
          case _ => 0
        }
      )
    }
  }

  private def buildTempModelSteps(model: ModelJson, basisTable: DeepTable): Seq[TempModelStep] = {
    if (model.steps.isDefined) {
      val tempSteps = model.steps.get.map(stepJson => {
        new TempModelStep(
          name = stepJson.name,
          join = stepJson.join,
          required = stepJson.required,
          parentName = stepJson.parent
        )
      })

      tempSteps.zipWithIndex.foreach { case (step, counter) =>
        step.tableAlias = Some(counter + 1)
      }
      addTableJoinsToSteps(tempSteps, None, basisTable)
      tempSteps
    } else {
      Seq.empty
    }
  }

  private def addTableJoinsToSteps(tempSteps: Seq[TempModelStep], parentStep: Option[TempModelStep], fromTable: DeepTable): Unit = {
    tempSteps.foreach { step =>
      if (step.tableJoin.isEmpty && parentStepMatches(step.parentName, parentStep)) {
        step.parent = parentStep
        step.tableJoin = fromTable.joins.get(step.join)
        val tableJoin = step.tableJoin.getOrElse(throw new TantalimException(s"Could not find join named `${step.join}` in `${fromTable.name}`", s"Found the following joins: ${fromTable.joins.keys}"))
        if (step.required.isEmpty) step.required = Some(tableJoin.required)
        val deepTable = compileTable(tableJoin.table.name)
        addTableJoinsToSteps(tempSteps, Some(step), deepTable)
      }
    }
  }

  private def parentStepMatches(parentName: Option[String], parentStep: Option[TempModelStep]): Boolean = {
    (parentName, parentStep) match {
      case (None, None) => true
      case (Some(_), Some(_)) => parentName.get == parentStep.get.name
      case _ => false
    }
  }

  private def compileOrderBy(orderBy: Option[Seq[ModelOrderBy]]): Seq[ModelOrderBy] = {
    if (orderBy.isEmpty) Seq.empty
    else {
      orderBy.get.map(o => new ModelOrderBy(o.fieldName, o.ascending))
    }
  }

  private def compileModelField(field: ModelFieldJson, basisColumn: TableColumn, step: Option[ModelStep]): ModelField = {
    new ModelField(
      name = field.name,
      basisColumn = basisColumn,
      step = step,
      required = field.required.getOrElse(basisColumn.required),
      updateable = field.updateable.getOrElse(basisColumn.updateable),
      fieldDefault = compileFieldDefault(field.fieldDefault),
      export = field.export.getOrElse(true)
    )
  }

  private def compileFieldDefault(fieldDefault: Option[FieldDefaultJson]): Option[FieldDefault] = {
    if (fieldDefault.isEmpty) None
    else {
      val o = fieldDefault.get
      Some(new FieldDefault(
        o.value,
        o.overwrite.getOrElse(false),
        compileFieldDefaultType(o.defaultType),
        o.watch.getOrElse(Seq.empty))
      )
    }
  }

  private def compileFieldDefaultType(fieldTypeJson: Option[String]): FieldDefaultType = {
    if (fieldTypeJson.isEmpty) FieldDefaultType.Constant
    else {
      val needle = fieldTypeJson.get.toLowerCase
      FieldDefaultType.values.find(t => t.toString.toLowerCase == needle).get
    }
  }

}

case class TempModelStep(name: String,
                         join: String,
                         parentName: Option[String],
                         var required: Option[Boolean],
                         var parent: Option[TempModelStep] = None,
                         var tableAlias: Option[Int] = None,
                         var tableJoin: Option[TableJoin] = None
                          )
