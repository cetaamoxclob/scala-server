package compiler

import com.tantalim.models._
import models.src.{FieldDefaultJson, ModelFieldJson, ModelJson}
import play.api.libs.json.{JsError, JsSuccess}
import services.ArtifactService

import scala.collection.{Seq, Map}

trait ModelCompiler extends ArtifactService with TableCompiler {

  def compileModel(name: String): Model = {
    println("Compiling model " + name)
    val json = getArtifactContentAndParseJson(ArtifactType.Model, name)
    json.validate[ModelJson] match {
      case JsSuccess(modelJson, _) =>
        compileModelView(modelJson.copy(name = Option(name)))
      case JsError(err) =>
        throw new Exception("Failed to compile model " + name + " due to the following error:" + err.toString)
    }
  }

  private def compileModelView(model: ModelJson): Model = {
    println("Compiling model view " + model.name.getOrElse("unknown"))
    val basisTable = getTableFromCache(model.basisTable).getOrElse {
      val newTable = compileTable(model.basisTable)
      addTableToCache(model.name.get, newTable)
      newTable
    }
    val instanceIdField = if (basisTable.primaryKey.isDefined) {
      model.fields.find(f =>
        f.basisColumn == basisTable.primaryKey.get.name // && f.step
      )
    } else None

    val tempSteps = buildModelSteps(model, basisTable)

    val stepsByInt: Map[Int, ModelStep] = tempSteps.map { step =>
      step.tableAlias.get -> new ModelStep(name = step.name,
        tableAlias = step.tableAlias.get,
        join = step.tableJoin.getOrElse(throw new Exception(s"tableJoin for $step is still missing")),
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

    new Model(
      model.name.get,
      basisTable,
      model.limit.getOrElse(0),
      instanceID = if (instanceIdField.isDefined) Option(instanceIdField.get.name) else None,
      fields = model.fields.map(f => {
        if (f.step.isDefined) {
          val stepName = f.step.get
          val stepID = stepIntsByName.getOrElse(
            stepName,
            throw new Exception(s"Can't find step `$stepName` for field `${f.name}`")
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
      children = model.children match {
        case Some(modelChildren) => modelChildren.map(childModel => {
          childModel.name.get -> compileModelView(childModel)
        }).toMap
        case None => Map.empty
      },
      parentLink = model.parentLink,
      steps = stepsByInt,
      orderBy = compileOrderBy(model.orderBy),
      allowInsert = model.allowInsert.getOrElse(basisTable.allowInsert),
      allowUpdate = model.allowUpdate.getOrElse(basisTable.allowUpdate),
      allowDelete = model.allowDelete.getOrElse(basisTable.allowDelete),
      preSave = model.preSave
    )
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
        if (step.required.isEmpty) step.required = Some(step.tableJoin.get.required)
        val tableJoin = step.tableJoin.getOrElse(throw new Exception(s"Could not find join named `${step.join}` in `${fromTable.name}`"))
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
      fieldDefault = compileFieldDefault(field.fieldDefault)
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
