package services

import java.io.File
import java.nio.charset.{StandardCharsets, Charset}
import java.nio.file.{StandardOpenOption, FileSystems, Files}

import com.tantalim.models.{Module, ArtifactType}
import com.tantalim.nodes._
import compiler.ModelCompiler
import play.api.libs.json._

class ArtifactExport(artifactType: ArtifactType) extends DataReader with DataSaver with ModelCompiler {
  val artifactReader = compileModel("~" + artifactType.toString.toLowerCase)

  def readFromDatabaseAndWriteToSource(module: String, artifactName: String) = {
    val artifactInstance = getArtifactInstanceFromDatabase(artifactName)
    val moduleName = artifactInstance.get("module")
    val jsObject = convertSmartNodeInstanceToJsObject(artifactInstance)
    writeToSource(moduleName, artifactName, Json.prettyPrint(jsObject))
  }

  private def getArtifactInstanceFromDatabase(artifactName: String): SmartNodeInstance = {
    val artifact = queryModelData(artifactReader, filter = Some(s"name = '$artifactName'"))
    artifact.rows.headOption.getOrElse(throw new Exception("Failed to find exactly one row matching = " + artifactName))
  }

  private def writeToSource(moduleName: Option[TntValue], artifactName: String, artifactContent: String) = {
    val module = if (moduleName.isDefined) moduleName.get.rawString
    else Module.default

    val moduleDirLocation = ArtifactService.tantalimRoot + (if (moduleName.isDefined) "/lib/" + moduleName.get.rawString else "/src") + "/" + artifactType.getDirectory

    val fileLocation = moduleDirLocation + "/" + artifactName + ".json"

    val artifactPath = FileSystems.getDefault.getPath(".", fileLocation)

    Files.write(artifactPath, artifactContent.getBytes(ArtifactService.charSet), StandardOpenOption.CREATE)
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
