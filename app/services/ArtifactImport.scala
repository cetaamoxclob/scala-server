package services

import data._
import models.{ArtifactType, Model, ModelField}
import play.api.libs.json._

class ArtifactImport(artifactType: ArtifactType) extends ArtifactCompilerService with DataReader with DataSaver {

  val artifactWriter = compileModel("~" + artifactType.toString.toLowerCase)

  def readFromSourceAndWriteToDatabase(artifactName: String) = {
    def deleteExistingArtifact() {
      val oldDataToDelete = queryModelData(artifactWriter, filter = Some("name = " + artifactName))
      if (!oldDataToDelete.isEmpty) {
        oldDataToDelete.deleteAll()
        saveAll(oldDataToDelete)
      }
    }
    deleteExistingArtifact()

    val artifactSource = getArtifactContentAndParseJson(artifactType, artifactName).as[JsObject]

    def insertArtifact(): SmartNodeInstance = {
      val smartNodeSet = new SmartNodeSet(artifactWriter.copy(name = artifactName))
      val smartInstance = smartNodeSet.insert
      def convertJsArrayToSmartNodeSet(smartNodeSet: SmartNodeSet, arraySource: JsArray): Unit = {
        arraySource.value.foreach {
          case JsObject(jsObject) => val smartInstance = smartNodeSet.insert
            convertJsObjectToSmartNodeInstance(smartInstance, JsObject(jsObject))
          case _ => throw new Exception("Expected JsObject")
        }
      }

      def convertJsObjectToSmartNodeInstance(smartInstance: SmartNodeInstance, objectSource: JsObject): Unit = {
        smartInstance.model.fields.foreach {
          case (fieldName: String, modelField: ModelField) =>
            val value = objectSource \ fieldName
            value match {
              case _: JsUndefined | JsNull => // Don't do anything
              case _ => smartInstance.set(fieldName, DataConverters.convertJsValueToTntValue(value))
            }
        }

        val artifactUniqueNameField = "name"
        if (smartInstance.get(artifactUniqueNameField).isEmpty) {
          smartInstance.set(artifactUniqueNameField, TntString(smartInstance.model.name))
        }

        smartInstance.model.children.foreach {
          case (childModelName: String, childModel: Model) =>
            val childSource = (objectSource \ childModelName).as[JsArray]
            println(childModelName + " -- " + childSource)
            val childSmartSet = new SmartNodeSet(childModel, parentInstance = Some(smartInstance))
            smartInstance.children += (childModelName -> childSmartSet)
            convertJsArrayToSmartNodeSet(childSmartSet, childSource)
        }
      }
      convertJsObjectToSmartNodeInstance(smartInstance, artifactSource)
      saveAll(smartNodeSet)
      smartInstance
    }
    insertArtifact()
  }

}
