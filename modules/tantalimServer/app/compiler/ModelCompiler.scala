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

  private def extendModel(model: ModelJson, parent: Option[Model]): Model = {
    println("Extending model " + model.extendModel.get)
    val superModel = parent.get
    superModel.copy(
      name = model.name.get,
      parentField = if (model.parentField.isDefined) model.parentField else Some(model.parentLink.get.parentField),
      childField = if (model.childField.isDefined) model.parentField else Some(model.parentLink.get.childField),
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

    val basisTable = getTableFromCache(model.basisTable.get).getOrElse {
      val newTable = compileTable(model.basisTable.get)
      addTableToCache(model.name.get, newTable)
      newTable
    }
    val instanceIdField = if (basisTable.primaryKey.isDefined && model.fields.isDefined) {
      model.fields.get.find(f =>
        f.basisColumn == basisTable.primaryKey.get.name // && f.step
      )
    } else None

    val tempSteps = buildModelSteps(model, basisTable)

    val stepsByInt: Map[Int, ModelStep] = tempSteps.map { step =>
      step.tableAlias.get -> new ModelStep(name = step.name,
        tableAlias = step.tableAlias.get,
        join = step.tableJoin.getOrElse(throw new TantalimException(s"tableJoin for $step is still missing", "Add or rename the step")),
        required = step.required.get,
        parentAlias = step.parent match {
          case Some(parent) => parent.tableAlias.get
          case _ => 0
        }
      )
    }.toMap
    val stepIntsByName: Map[String, Int] = tempSteps.map { step =>
      step.name -> step.tableAlias.get
    }.toMap

    println("Sending Model " + model.name.get)
    val newModel = new Model(
      model.name.get,
      basisTable,
      model.limit.getOrElse(0),
      instanceID = if (instanceIdField.isDefined) Option(instanceIdField.get.name) else None,
      fields = if (model.fields.isEmpty) Map.empty
      else model.fields.get.map(f => {
        if (f.step.isDefined) {
          val stepName = f.step.get
          val stepID = stepIntsByName.getOrElse(
            stepName,
            throw new TantalimException(s"Can't find step `$stepName` for field `${f.name}`", "Add it")
          )
          val fieldStep = stepsByInt.get(stepID)
          val tableJoin = fieldStep.get.join

          f.name -> compileModelField(f,
            tableJoin.table.getColumn(f.basisColumn),
            fieldStep
          )
        } else {
          f.name -> compileModelField(f, basisTable.getColumn(f.basisColumn), None)
        }
      }).toMap,
      parentField = if (model.parentLink.isDefined) {
        println(s"Warning parentLink in ${model.name.get} was deprecated and should be replaced with parentField and childField")
        Some(model.parentLink.get.parentField)
      } else model.parentField,
      childField = if (model.parentLink.isDefined) Some(model.parentLink.get.childField) else model.childField,
      parent = parent,
      steps = stepsByInt,
      orderBy = compileOrderBy(model.orderBy),
      allowInsert = model.allowInsert.getOrElse(basisTable.allowInsert),
      allowUpdate = model.allowUpdate.getOrElse(basisTable.allowUpdate),
      allowDelete = model.allowDelete.getOrElse(basisTable.allowDelete),
      preSave = model.preSave,
      filter = model.filter
    )
    if (model.parentLink.isDefined) {
      val childField = newModel.fields.getOrElse(model.parentLink.get.childField,
        throw new TantalimException(s"Could not find childField named ${model.parentLink.get.childField}",
          "Model has the following fields: " + newModel.fields))
      val parentField = parent.get.fields.getOrElse(model.parentLink.get.parentField,
        throw new TantalimException(s"Could not find parentField named ${model.parentLink.get.parentField}",
          "Model has the following fields: " + parent.get.fields))
      if (childField.dataType != parentField.dataType) {
        throw new TantalimException(s"parent and child fields are not of the same data type in ${model.name.get}",
          s"${childField.name} is a ${childField.dataType} and ${parentField.name} is a ${parentField.dataType}")
      }
    }
    if (model.children.isDefined) {
      model.children.get.foreach { childJson =>
        val childModel =
          if (childJson.extendModel.isDefined) extendModel(childJson, Some(newModel))
          else compileModelView(childJson, Some(newModel))
        if (childModel.parentField.isEmpty) {
          // We could try to guess based on the parent model's identifier field
          throw new TantalimException(s"Child model named ${childModel.name} is missing parentField", "")
        }
        if (childModel.childField.isEmpty) {
          throw new TantalimException(s"Child model named ${childModel.name} is missing childField", "")
        }
        // TODO check to make sure the parentField actually exists
        // TODO check to make sure the childField actually exists
        newModel.children(childModel.name) = childModel
      }
    }
    newModel
  }

  private def buildModelSteps(model: ModelJson, basisTable: DeepTable): Seq[TempModelStep] = {
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
