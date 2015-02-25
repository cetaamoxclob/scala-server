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

    val smartNodeSet = new SmartNodeSet(artifactWriter.copy(name = artifactName))
    val smartInstance = smartNodeSet.insert
    convertJsObjectToSmartNodeInstance(smartInstance, artifactSource)
    insertSingleRow(smartInstance)
    smartInstance
  }

  private def convertJsArrayToSmartNodeSet(smartNodeSet: SmartNodeSet, arraySource: JsArray): Unit = {
    arraySource.value.foreach {
      case JsObject(jsObject) => val smartInstance = smartNodeSet.insert
        convertJsObjectToSmartNodeInstance(smartInstance, JsObject(jsObject))
      case _ => throw new Exception("Expected JsObject")
    }
  }

  private def convertJsObjectToSmartNodeInstance(smartInstance: SmartNodeInstance, objectSource: JsObject): Unit = {
    smartInstance.model.fields.foreach {
      case (fieldName: String, modelField: ModelField) => {
        val value = objectSource \ fieldName
        value match {
          case _: JsUndefined | JsNull => // Don't do anything
          case _ => smartInstance.set(fieldName, DataConverters.convertJsValueToTntValue(value))
        }
      }
    }

    val artifactUniqueNameField = "name"
    if (smartInstance.get(artifactUniqueNameField).isEmpty) {
      smartInstance.set(artifactUniqueNameField, TntString(smartInstance.model.name))
    }

    smartInstance.model.children.foreach {
      case (childModelName: String, childModel: Model) => {
        val childSource = (objectSource \ childModelName).as[JsArray]
        println(childModelName + " -- " + childSource)
        val childSmartSet = new SmartNodeSet(childModel, parentInstance = Some(smartInstance))
        smartInstance.children += (childModelName -> childSmartSet)
        convertJsArrayToSmartNodeSet(childSmartSet, childSource)
      }
    }
  }

}
