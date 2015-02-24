package services

import data._
import models.{ModelField, ArtifactType, Model}
import play.api.libs.json._

import scala.util.Success

class ArtifactImport(artifactType: ArtifactType) extends ArtifactCompilerService with DataReader with DataSaver {

  val artifactWriter = compileModel("~" + artifactType.toString.toLowerCase)

  def readFromSourceAndWriteToDatabase(artifactName: String) = {
    val oldDataToDelete = queryOneRow(artifactWriter, Some("name = " + artifactName))
    if (oldDataToDelete.isDefined) {
      deleteSingleRow(oldDataToDelete.get)
    }

    val artifactSource = getArtifactContentAndParseJson(artifactType, artifactName).as[JsObject]

    val dataToInsert = convertSourceToInsertData(artifactWriter.copy(name = artifactName), artifactSource)
    saveAll(dataToInsert)
    dataToInsert
  }

  private def convertSourceToInsertData(model: Model, artifactSource: JsObject): SmartNodeSet = {
    val smartNodeSet = new SmartNodeSet(model)
    val smartInstance = smartNodeSet.insert

    {
      val artifactUniqueNameField = "name"
      smartInstance.set(artifactUniqueNameField, TntString(smartInstance.model.name))
      smartInstance.model.fields.foreach{
        case(fieldName: String, modelField: ModelField) => {
          val value = (artifactSource \ fieldName)
          smartInstance.set(fieldName, convertJsValueToTntValue(value))
        }
      }
      smartInstance.model.children.foreach{
        case(childModelName: String, childModel: Model) => {
          val childSource = (artifactSource \ childModelName).as[JsObject]
          val childSmartSet = convertSourceToInsertData(childModel, childSource)
          smartInstance.children + (childModelName -> childSmartSet)
        }
      }
    }
    smartNodeSet
  }

  private def convertJsValueToTntValue(jsValue: JsValue): TntValue = {
    jsValue match {
      case JsString(value) => TntString(value)
      case JsNumber(value) => TntDecimal(value)
      case JsBoolean(value) => TntBoolean(value)
      case JsNull => new TntNull
      case _ => throw new Exception("Can't parse JsValue to TntValue")
    }
  }
}
