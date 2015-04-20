package services

import com.tantalim.nodes.{TntString, SmartNodeSet, SmartNodeInstance}
import controllers.core.{PlayableDatabaseConnection, DataConverters}
import com.tantalim.artifacts.compiler.ModelCompiler
import com.tantalim.database.services.{DataSaver, DataReader}
import com.tantalim.models.{Model, ModelField}
import play.api.libs.json._

class ArtifactImport(artifactType: String) extends DataReader with DataSaver with ModelCompiler with PlayableDatabaseConnection {

  val artifactWriter = compileModel(artifactType)

  def readFromSourceAndWriteToDatabase(module: String, artifactName: String) = {
    deleteExistingArtifactFromDatabase(artifactName)
    insertArtifactIntoDatabase(module, artifactName)
  }

  private def deleteExistingArtifactFromDatabase(artifactName: String) {
    val oldDataToDelete = queryModelData(artifactWriter, filter = Some(s"name = '$artifactName'"))
    if (!oldDataToDelete.isEmpty) {
      oldDataToDelete.deleteAll()
      saveAll(oldDataToDelete)
    }
  }

  private def insertArtifactIntoDatabase(module: String, artifactName: String): SmartNodeInstance = {
    val smartNodeSet = new SmartNodeSet(artifactWriter.copy(name = artifactName))
    val smartInstance = smartNodeSet.insert

    val artifactSource = getArtifactContentAndParseJson(artifactType, artifactName).as[JsObject]
    convertJsObjectToSmartNodeInstance(smartInstance, artifactSource)

    smartInstance.set("name", TntString(artifactName))
    smartInstance.set("module", TntString(module))

    saveAll(smartNodeSet)
    smartInstance
  }

  private def convertJsObjectToSmartNodeInstance(smartInstance: SmartNodeInstance, objectSource: JsObject): Unit = {
    smartInstance.model.fields.foreach {
      case (fieldName: String, modelField: ModelField) =>
        val value = objectSource \ fieldName
        value match {
          case _: JsUndefined | JsNull => // Don't do anything
          case _ => smartInstance.set(fieldName, DataConverters.convertJsValueToTntValue(value))
        }
    }

    smartInstance.model.children.foreach {
      case (childModelName: String, childModel: Model) =>
        objectSource \ childModelName match {
          case _: JsUndefined | JsNull => // Don't do anything
          case childSource: JsArray =>
            println(childModelName + " -- " + childSource)
            val childSmartSet = new SmartNodeSet(childModel, parentInstance = Some(smartInstance))
            smartInstance.children += (childModelName -> childSmartSet)
            convertJsArrayToSmartNodeSet(childSmartSet, childSource)
          case _ => throw new Exception("Something else happened that shouldn't")
        }
    }
  }

  private def convertJsArrayToSmartNodeSet(smartNodeSet: SmartNodeSet, arraySource: JsArray): Unit = {
    arraySource.value.foreach {
      case JsObject(jsObject) => val smartInstance = smartNodeSet.insert
        convertJsObjectToSmartNodeInstance(smartInstance, JsObject(jsObject))
      case _ => throw new Exception("Expected JsObject")
    }
  }


}
