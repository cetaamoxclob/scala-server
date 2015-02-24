package services

import data._
import models.{Model, ArtifactType}
import play.api.libs.json._

import scala.util.{Try, Success}

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

  def convertSourceToInsertData(model: Model, artifactName: String, artifactSource: JsObject): DataInstance = {
    var dataMap = artifactSource.value.toMap
    dataMap += ("name" -> JsString(artifactName))
    val childDataInstances = if (model.children.isEmpty) None
    else {
      val childDataJson: Map[String, Seq[DataInstance]] = model.children.transform {
        case (childModelName, childModel) => {
          val dataResult: Seq[DataInstance] = dataMap.get(childModelName) match {
            case Some(JsArray(childData)) => {
              childData.map { childRow: JsValue =>
                childRow match {
                  case JsObject(childRowObject) => {
                    convertSourceToInsertData(model.children.get(childModelName).get, childModelName, JsObject(childRowObject))
                  }
                  case _ => throw new Exception("Got something other than an JsObject")
                }
              }
            }
            case Some(_) | None => throw new Exception("Got something other than an JsArray for " + dataMap.get(childModelName))
          }
          dataMap -= childModelName
          dataResult
        }
      }
      //      dataMap += ("children" -> JsObject(childDataJson.toSeq))
      Some(childDataJson)
    }
    new DataInstance(
      state = DataState.Inserted,
      data = Some(dataMap),
      children = childDataInstances)
  }

}
