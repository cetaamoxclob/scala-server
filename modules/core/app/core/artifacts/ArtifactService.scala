package core.artifacts

import java.io.File
import java.nio.charset.StandardCharsets

import com.google.common.base.Charsets
import com.google.common.io.Files
import com.tantalim.models.{ArtifactStub, ArtifactType, Module}
import com.tantalim.util.TantalimException
import core.compiler.src.{TableJson, ModelJson, PageJson, MenuJson}
import play.api.Play
import play.api.Play.current
import play.api.libs.json._

trait ArtifactService {

  case class SourceLocation(filePath: String, module: String)

  private def getSourceLocation(artifactType: ArtifactType, name: String): SourceLocation = {
    val fileNameAndPartialDirLocation = artifactType.getDirectory + "/" + name + ".json"
    val srcDir = ArtifactService.tantalimRoot + "/src/" + fileNameAndPartialDirLocation
    if (fileExists(srcDir)) SourceLocation(srcDir, Module.default)
    else {
      val libDirLocation = ArtifactService.tantalimRoot + "/lib/"
      val libDir = new File(libDirLocation)
      val libList: Array[SourceLocation] = libDir.listFiles.flatMap(d => {
        val libSrcDir = d.toString + "/" + fileNameAndPartialDirLocation
        val moduleName = d.toString.substring(libDirLocation.length)
        if (d.isDirectory && fileExists(libSrcDir)) Some(SourceLocation(filePath = libSrcDir, module = moduleName))
        else None
      })

      if (libList.length == 0) {
        throw new MissingArtifactException(artifactType, name)
      }
      if (libList.length > 1) {
        println(s"WARN: $artifactType $name was found in more than one location. Using the 'first'")
      }

      libList.headOption.get
    }
  }

  private def fileExists(fileLocation: String): Boolean = {
    val f = new File(fileLocation)
    f.exists() && f.isFile
  }

  def getArtifactContentAndParseJson(artifactType: ArtifactType, name: String): JsValue = {
    val sourceLocation = getSourceLocation(artifactType, name)
    val artifactContent = Files.toString(Play.getFile(sourceLocation.filePath), Charsets.UTF_8)

    if (artifactContent.isEmpty)
      throw new TantalimException(s"Artifact $artifactType named `$name` is empty", "Edit the file: " + sourceLocation.filePath + artifactContent)

    try {
      val result = Json.parse(artifactContent).asInstanceOf[JsObject]
      result + ("module" -> JsString(sourceLocation.module))
    } catch {
      case e: Exception => throw new TantalimException(
        s"Failed to parse json for $artifactType named `$name`",
        e.getMessage
      )
    }
  }

  def getMenu(name: String): JsResult[MenuJson] = {
    val artifactJson = getArtifactContentAndParseJson(ArtifactType.Menu, name)
    artifactJson.validate[MenuJson]
  }

  def getPage(name: String): JsResult[PageJson] = {
    val artifactJson = getArtifactContentAndParseJson(ArtifactType.Page, name)
    artifactJson.validate[PageJson]
  }

  def getModel(name: String): JsResult[ModelJson] = {
    val artifactJson = getArtifactContentAndParseJson(ArtifactType.Model, name)
    artifactJson.validate[ModelJson]
  }

  def getTable(name: String): JsResult[TableJson] = {
    val artifactJson = getArtifactContentAndParseJson(ArtifactType.Table, name)
    artifactJson.validate[TableJson]
  }

  @deprecated
  def findArtifacts: Seq[ArtifactStub] = {
    def artifactName(rootPath: String, filePath: String): String = {
      filePath.replace(rootPath, "").replace(".json", "").replace("/", "")
    }

    def getArtifactsFromDir(moduleLocation: String, artifactType: ArtifactType, moduleName: Option[String]): Seq[ArtifactStub] = {
      val artifactDir = new File(moduleLocation + "/" + artifactType.getDirectory)
      if (artifactDir.isDirectory) {
        artifactDir.listFiles().map(f =>
          ArtifactStub(artifactType, artifactName(artifactDir.getAbsolutePath, f.getCanonicalPath), moduleName.getOrElse(Module.default))
        )
      } else Seq.empty
    }

    ArtifactType.values().flatMap { artifactType: ArtifactType =>
      val localFiles = getArtifactsFromDir(ArtifactService.tantalimRoot + "/src", artifactType, None)

      new File(ArtifactService.tantalimRoot + "/lib/").listFiles().foldLeft(localFiles) { (acc, libDir) =>
        if (libDir.isDirectory) {
          val localFiles = getArtifactsFromDir(libDir.getCanonicalPath, artifactType, Some(libDir.getName))
          acc ++ localFiles
        } else acc
      }
    }.toSeq
  }

}

object ArtifactService {
  val charSet = StandardCharsets.UTF_8
  val tantalimRoot = "tantalim"
}