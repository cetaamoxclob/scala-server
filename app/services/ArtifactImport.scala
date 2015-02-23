package services

import data.DataState
import models.ArtifactType
import play.api.libs.json.JsValue

import scala.util.{Try, Success}

object ArtifactImport {
  def convertFromSelectedDataToSaveData(selectDataRow: SelectDataRow): DataRow = {
    val childData: Option[Map[String, Seq[DataRow]]] = if (selectDataRow.children.isDefined) {
      val childData2: Map[String, Seq[DataRow]] = selectDataRow.children.get.transform { (childName: String, selectDataRows: Seq[SelectDataRow]) =>
        val returned: Seq[DataRow] = selectDataRows.map { childDataRow => convertFromSelectedDataToSaveData(childDataRow)}
        returned
      }
      Some(childData2)
    } else None

    new DataRow(
      state = DataState.Deleted,
      data = Some(selectDataRow.data),
      id = selectDataRow.id,
      children = childData
    )
  }
}

class ArtifactImport(artifactType: ArtifactType) extends ArtifactCompilerService with DataReader with DataSaver {

  val artifactWriter = compileModel("~" + artifactType.toString.toLowerCase)

  def readFromSourceAndWriteToDatabase(artifactName: String): Try[String] = {
    val artifactSource = getArtifactContentAndParseJson(artifactType, artifactName)
    val oldDataToDelete = queryOneRow(artifactWriter, Some("name = " + artifactName))
    if (oldDataToDelete.isDefined) {
      val dataRow = ArtifactImport.convertFromSelectedDataToSaveData(oldDataToDelete.get)
      deleteSingleRow(artifactWriter, dataRow)
    }

    val dataToInsert = convertSourceToInsertData(artifactSource)

    Success(oldDataToDelete.toString())
  }

  def convertSourceToInsertData(artifactSource: JsValue): DataRow = {

    val dataMap = Map.empty[String, JsValue]
    new DataRow(state = DataState.Inserted, data = Some(dataMap))
  }

}
