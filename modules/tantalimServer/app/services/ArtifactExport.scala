package services

import com.tantalim.models.ArtifactType
import com.tantalim.nodes._
import compiler.ModelCompiler
import data._
import play.api.libs.json._

class ArtifactExport(artifactType: ArtifactType) extends DataReader with DataSaver with ModelCompiler {
  val artifactReader = compileModel("~" + artifactType.toString.toLowerCase)

  def readFromDatabaseAndWriteToSource(artifactName: String) = {
    val artifact = queryModelData(artifactReader, filter = Some(s"name = '$artifactName'"))
    val firstArtifact = artifact.rows.headOption.getOrElse(throw new Exception("Failed to find exactly one row matching = " + artifactName))
    convertSmartNodeInstanceToJsObject(firstArtifact)
  }

  private def convertSmartNodeInstanceToJsObject(instance: SmartNodeInstance): JsObject = {
    val model = instance.model
    val jsonFields = model.fields.values.map { field =>
      if (field.export) {
        val fieldValue = instance.get(field.name)
        val returnVal: (String, JsValue) = if (fieldValue.isDefined) {
          field.name -> (fieldValue.get match {
            case TntString(raw) => if (raw == null) JsNull else JsString(raw)
            case TntInt(raw) => JsNumber(raw.toInt)
            case TntDecimal(raw) => JsNumber(raw)
            case TntBoolean(raw) => JsBoolean(raw)
            case TntNull() => JsNull
            case value => JsString(value.toString)
          })
        } else {
          field.name -> JsNull
        }
        returnVal
      } else {
        field.name -> JsNull
      }
    }.toSeq.filter { case (fieldName, value) =>
      value != JsNull
    }

    val jsonChildren = model.children.map { case (childName, childModel) =>
      val childSet = instance.children.get(childName)
      val returnVal: (String, JsValue) = if (childSet.isEmpty) childName -> JsNull
      else childName -> {
        JsArray(childSet.get.rows.map { childRow =>
          convertSmartNodeInstanceToJsObject(childRow)
        })
      }
      returnVal
    }.toSeq.filter {
      case (_, value: JsArray) => value.value.nonEmpty
      case (_, _) => false
    }

    JsObject(jsonFields ++ jsonChildren)
  }
}
