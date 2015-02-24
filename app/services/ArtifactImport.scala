package services

import data._
import models.{ArtifactType, Model}
import play.api.libs.json._

import scala.util.Success

class ArtifactImport(artifactType: ArtifactType) extends ArtifactCompilerService with DataReader with DataSaver {

  val artifactWriter = compileModel("~" + artifactType.toString.toLowerCase)

  def readFromSourceAndWriteToDatabase(artifactName: String) = {
    val oldDataToDelete = queryOneRow(artifactWriter, Some("name = " + artifactName))
    if (oldDataToDelete.isDefined) {
      deleteSingleRow(artifactWriter, oldDataToDelete.get)
    }

    val artifactSource = getArtifactContentAndParseJson(artifactType, artifactName).as[JsObject]
    val dataToInsert = convertSourceToInsertData(artifactWriter, artifactName, artifactSource)
    println(dataToInsert)
    val result = insertSingleRow(artifactWriter, dataToInsert)

    Success(result)
  }

  def convertSourceToInsertData(model: Model, modelName: String, artifactSource: JsObject): DataInstance = {
    val instanceData = artifactSource.value.toMap
    new DataInstance(
      state = DataState.Inserted,
      data = getInstanceData(model, modelName: String, instanceData),
      children = getChildData(model, instanceData))
  }

  private def getInstanceData(model: Model, modelName: String, dataMap: Map[String, JsValue]) = {
    var fixedInstanceData = dataMap + (("name", JsString(modelName)))
    model.children.keys.foreach(childModelName => fixedInstanceData -= childModelName)
    Some(fixedInstanceData)
  }

  private def getChildData(model: Model, dataMap: Map[String, JsValue]) = {
    if (model.children.isEmpty) None
    else {
      // TODO Super messy! Need to cleanup. Can we use a reader and apply method here?
      val childDataJson: Map[String, Seq[DataInstance]] = model.children.transform {
        case (childModelName, childModel) =>
          val dataResult = dataMap.get(childModelName) match {
            case Some(JsArray(childData)) =>
              childData.map { childRow: JsValue =>
                childRow match {
                  case JsObject(childRowObject) =>
                    convertSourceToInsertData(model.children.get(childModelName).get, childModelName, JsObject(childRowObject))
                  case _ => throw new Exception("Got something other than an JsObject")
                }
              }
            // None might be a valid option if the child data is optional (like Joins in Table)
            case None => Seq.empty[DataInstance]
          }
          dataResult
      }
      Some(childDataJson)
    }
  }

}
