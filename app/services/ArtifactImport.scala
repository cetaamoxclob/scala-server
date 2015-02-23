package services

import data._
import models.ArtifactType
import play.api.libs.json.JsValue

import scala.util.{Try, Success}

class ArtifactImport(artifactType: ArtifactType) extends ArtifactCompilerService with DataReader with DataSaver {

  val artifactWriter = compileModel("~" + artifactType.toString.toLowerCase)

  def readFromSourceAndWriteToDatabase(artifactName: String): Try[String] = {
    val artifactSource = getArtifactContentAndParseJson(artifactType, artifactName)
    val oldDataToDelete = queryOneRow(artifactWriter, Some("name = " + artifactName))
    if (oldDataToDelete.isDefined) {
      deleteSingleRow(artifactWriter, oldDataToDelete.get)
    }

    val dataToInsert = convertSourceToInsertData(artifactSource)

    Success(oldDataToDelete.toString())
  }

  def convertSourceToInsertData(artifactSource: JsValue): DataInstance = {

    val dataMap = Map.empty[String, JsValue]
    new DataInstance(state = DataState.Inserted, data = Some(dataMap))
  }

}
