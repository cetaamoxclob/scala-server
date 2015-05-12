package com.tantalim.artifacts.compiler

import com.tantalim.artifacts.ArtifactService
import com.tantalim.artifacts.json.{ModelStepJson, ModelFieldJson, ModelJson}
import com.tantalim.models._
import com.tantalim.util.TantalimException
import play.api.libs.json.{JsError, JsSuccess}

import scala.collection.{Map, Seq}

trait ModelCompiler extends ArtifactService with TableCompiler {

  def compileModel(name: String): Model = {
    //    println("Compiling modelJson set " + name)
    compileModelView(getModelJson(name))
  }

  def getModelJson(name: String): ModelJson = {
    val json = getArtifactContentAndParseJson(ModelCompiler.artifactName, name)
    json.validate[ModelJson] match {
      case JsSuccess(modelJson, _) =>
        modelJson.copy(name = Option(name))
      case JsError(err) =>
        throw new TantalimException("Failed to compile modelJson " + name, "See the following error:" + err.toString)
    }
  }

  // TODO This class does 2 things! split getModelJson and compileModelView after we finish logic for getting from the database
  def compileModelView(modelJson: ModelJson, parent: Option[Model] = None): Model = {
    if (modelJson.name.isEmpty) {
      throw new TantalimException("Model Name is missing", "Add name to " + modelJson)
    }
    //    println("Compiling modelJson " + modelJson.name.get)
    val basisTable = getBasisTable(modelJson.basisTable)
    val steps = buildModelSteps(modelJson, basisTable)
    val fieldsFromSteps = convertJsonFieldsFromStep(modelJson.steps, steps)
    val modelFields = convertJsonFieldsToModelFieldMap(modelJson.fields, basisTable, None)

    val newModel = createModel(modelJson, basisTable, modelFields ++ fieldsFromSteps, parent, steps)
    if (newModel.parent.isDefined && !newModel.basisTable.isMock) checkChildModel(newModel)
    if (modelJson.children.isDefined) {
      modelJson.children.get.foreach { childJson =>
        addChildModelToParentModel(childJson, newModel)
      }
    }
    newModel
  }

  private def extendModel(modelJson: ModelJson, parent: Option[Model]): Model = {
    //    println("Extending modelJson " + modelJson.extendModel.get)
    val superModel = parent.get
    val modelFields = superModel.fields ++
      convertJsonFieldsToModelFieldMap(modelJson.fields, superModel.basisTable, None) ++
      convertJsonFieldsFromStep(modelJson.steps, superModel.steps.values.toSeq)

    superModel.copy(
      name = modelJson.name.get,
      parentField = modelJson.parentField,
      childField = modelJson.childField,
      fields = modelFields,
      parent = parent,
      filter = if (modelJson.filter.isDefined) modelJson.filter else superModel.filter,
      preSave = modelJson.preSave
    )
  }

  private def createModel(modelJson: ModelJson, basisTable: DeepTable, modelFields: Map[String, ModelField], parent: Option[Model], steps: Seq[ModelStep]): Model = {
    //    println("Creating new Model " + modelJson.name.get)
    new Model(
      modelJson.name.get,
      basisTable,
      modelJson.limit.getOrElse(0),
      fields = modelFields.toMap,
      instanceID = findInstanceIdField(basisTable.primaryKey, modelFields.values),
      parentField = modelJson.parentField,
      childField = modelJson.childField,
      parent = parent,
      steps = ModelCompiler.convertStepsToMap(steps),
      orderBy = compileOrderBy(modelJson.orderBy),
      allowInsert = modelJson.allowInsert.getOrElse(basisTable.allowInsert),
      allowUpdate = modelJson.allowUpdate.getOrElse(basisTable.allowUpdate),
      allowDelete = modelJson.allowDelete.getOrElse(basisTable.allowDelete),
      preSave = modelJson.preSave,
      filter = modelJson.filter,
      customUrlSource = modelJson.customUrlSource
    )
  }

  private def findInstanceIdField(primaryKey: Option[TableColumn], modelFields: Iterable[ModelField]): Option[ModelField] = {
    if (primaryKey.isDefined) {
      modelFields.find(f =>
        f.basisColumn.isDefined
          && f.basisColumn.get.name == primaryKey.get.name
          && f.step.isEmpty
      )
    } else None
  }

  private def getBasisTable(tableName: String): DeepTable = {
    if (Table.isMock(tableName)) {
      Table.createMock
    } else {
      getTableFromCache(tableName).getOrElse {
        val newTable = compileTable(tableName)
        addTableToCache(newTable.name, newTable)
        newTable
      }
    }
  }

  private def checkChildModel(childModel: Model): Unit = {
    if (childModel.parentField.isEmpty) {
      // We could try to guess based on the parent modelJson's identifier field ???
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

  private def buildModelSteps(modelJson: ModelJson, basisTable: DeepTable): Seq[ModelStep] = {
    if (modelJson.steps.isEmpty || modelJson.steps.get.isEmpty) return Seq.empty

    case class TempModelStep(json: ModelStepJson,
                             tableJoin: TableJoin,
                             var steps: Seq[TempModelStep])

    def buildTempModelSteps(stepsJson: Seq[ModelStepJson],
                            fromTable: DeepTable,
                            parentStepJson: Option[TempModelStep]): Seq[TempModelStep] = {
      val tempSteps = stepsJson.map(stepJson => {
        val tableJoin = fromTable.joins.getOrElse(stepJson.join,
          throw new TantalimException(
            s"Could not find join named `${stepJson.join}` in `${fromTable.name}`",
            s"Found the following joins: ${fromTable.joins.keys}")
        )
        val thisStep = new TempModelStep(stepJson, tableJoin, steps = Seq.empty)
        if (stepJson.steps.isDefined && stepJson.steps.get.nonEmpty) {
          val deepTable = getBasisTable(tableJoin.table.name)
          new TempModelStep(stepJson, tableJoin,
            steps = buildTempModelSteps(stepJson.steps.get, deepTable, Some(thisStep))
          )
        } else new TempModelStep(stepJson, tableJoin, Seq.empty)
      })
      tempSteps
    }

    val tempSteps = buildTempModelSteps(modelJson.steps.get, basisTable, None)

    val stepBuilder = Seq.newBuilder[ModelStep]
    def addSteps(steps: Seq[TempModelStep], parentAlias: Int): Unit = {
      var tableAlias = stepBuilder.result().size
      steps.foreach(step => {
        tableAlias = tableAlias + 1
        stepBuilder += new ModelStep(
          name = step.json.name,
          tableAlias = tableAlias,
          join = step.tableJoin,
          required = step.json.required.getOrElse(step.tableJoin.required),
          parentAlias = parentAlias
        )
        addSteps(step.steps, tableAlias)
      })
    }
    addSteps(tempSteps, 0)
    stepBuilder.result()
  }

  private def convertJsonFieldsToModelFieldMap(modelFields: Option[Seq[ModelFieldJson]], basisTable: Table, fieldStep: Option[ModelStep]): Map[String, ModelField] = {
    if (modelFields.isEmpty) return Map.empty

    modelFields.get.map(f =>
      f.name -> compileModelField(f, ModelCompiler.findColumn(basisTable, f.basisColumn), fieldStep)
    ).toMap
  }

  private def convertJsonFieldsFromStep(modelStepsJson: Option[Seq[ModelStepJson]], steps: Seq[ModelStep]): Map[String, ModelField] = {
    if (modelStepsJson.isEmpty) return Map.empty

    modelStepsJson.get.flatMap(step => {
      val fieldStep = Some(ModelCompiler.findStepByName(step.name, steps))
      val basisTable = fieldStep.get.join.table

      val fieldsOnThisStep = convertJsonFieldsToModelFieldMap(step.fields, basisTable, fieldStep)
      val fieldsOnChildSteps = convertJsonFieldsFromStep(step.steps, steps)

      fieldsOnThisStep ++ fieldsOnChildSteps
    }
    ).toMap
  }

  private def compileOrderBy(orderBy: Option[Seq[ModelOrderBy]]): Seq[ModelOrderBy] = {
    if (orderBy.isEmpty) Seq.empty
    else {
      orderBy.get.map(o => new ModelOrderBy(o.fieldName, o.ascending))
    }
  }

  private def compileModelField(field: ModelFieldJson, basisColumn: Option[TableColumn], step: Option[ModelStep]): ModelField = {
    val alwaysDefault = field.alwaysDefault.getOrElse(false)
    new ModelField(
      name = field.name,
      basisColumn = basisColumn,
      dataType = {
        if (field.dataType.isDefined) TableCompiler.compileDataType(field.dataType)
        else if (basisColumn.isDefined) basisColumn.get.dataType
        else TableCompiler.compileDataType(field.dataType)
      },
      step = step,
      required = field.required.getOrElse(if (basisColumn.isDefined) basisColumn.get.required else false),
      updateable = if (alwaysDefault) false
      else field.updateable.getOrElse(if (basisColumn.isDefined) basisColumn.get.updateable else false),
      alwaysDefault = alwaysDefault,
      fieldDefault = field.fieldDefault,
      functionDefault = field.functionDefault,
      valueDefault = field.valueDefault,
      export = field.export.getOrElse(true)
    )
  }
}

object ModelCompiler {
  val artifactName = "models"

  def findColumn(table: Table, name: Option[String]): Option[TableColumn] = {
    if (name.isEmpty) None
    else Some(table.columns.getOrElse(
      name.get,
      throw new TantalimException(f"failed to find column named `$name` in table `${table.name}`", s"found: ${table.columns.keys}")
    ))
  }

  def findStepByName(stepName: String, steps: Seq[ModelStep]): ModelStep = {
    steps.find(step => step.name == stepName).getOrElse(
      throw new TantalimException(s"Can't find step `$stepName`", s"Existing steps are: ${steps.mkString(", ")}")
    )
  }

  def convertStepsToMap(steps: Seq[ModelStep]): Map[Int, ModelStep] = {
    steps.map { step =>
      step.tableAlias -> step
    }.toMap
  }


}

