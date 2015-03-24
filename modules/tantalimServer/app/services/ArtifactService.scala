package services

import java.io.File

import com.google.common.base.Charsets
import com.google.common.io.Files
import com.tantalim.models.{ArtifactStub, ArtifactType}
import com.tantalim.util.TantalimException
import compiler.src._
import play.api.Play.current
import play.api.Play
import play.api.libs.json._

trait ArtifactService {

  private def tantalimRoot = "tantalim"

  private def getSourceLocation(artifactType: ArtifactType, name: String): String = {
    val fileNameAndPartialDirLocation = artifactType.getDirectory + "/" + name + ".json"
    val srcDir = tantalimRoot + "/src/" + fileNameAndPartialDirLocation
    if (fileExists(srcDir)) srcDir
    else {
      val libDirLocation = tantalimRoot + "/lib/"
      val libDir = new File(libDirLocation)
      val libList: Array[String] = libDir.listFiles.flatMap(d => {
        val libSrcDir = d.toString + "/" + fileNameAndPartialDirLocation
        if (d.isDirectory && fileExists(libSrcDir)) Some(libSrcDir)
        else None
      })

      libList.headOption.getOrElse {
        throw new MissingArtifactException(artifactType, name)
      }
    }
  }

  private def fileExists(fileLocation: String): Boolean = {
    val f = new File(fileLocation)
    f.exists() && f.isFile
  }

  def getArtifactContentAndParseJson(artifactType: ArtifactType, name: String): JsValue = {
    val fileName = getSourceLocation(artifactType, name)
    val artifactContent = Files.toString(Play.getFile(fileName), Charsets.UTF_8)

    if (artifactContent.isEmpty)
      throw new TantalimException(s"Artifact $artifactType named `$name` is empty", "Edit the file: " + fileName + artifactContent)

    try {
      Json.parse(artifactContent)
    } catch {
      case e: Exception => throw new TantalimException(
        s"Failed to parse $artifactType named `$name`",
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

  def findArtifacts: Seq[ArtifactStub] = {
    def artifactName(rootPath: String, filePath: String): String = {
      filePath.replace(rootPath, "").replace(".json", "").replace("/", "")
    }

    def getArtifactsFromDir(moduleLocation: String, artifactType: ArtifactType, moduleName: Option[String]): Seq[ArtifactStub] = {
      val artifactDir = new File(moduleLocation + "/" + artifactType.getDirectory)
      if (artifactDir.isDirectory) {
        artifactDir.listFiles().map(f =>
          ArtifactStub(artifactType, artifactName(artifactDir.getAbsolutePath, f.getCanonicalPath), moduleName)
        )
      } else Seq.empty
    }

    ArtifactType.values().flatMap { artifactType: ArtifactType =>
      val localFiles = getArtifactsFromDir(tantalimRoot + "/src", artifactType, None)

      new File(tantalimRoot + "/lib/").listFiles().foldLeft(localFiles){(acc, libDir) =>
        if (libDir.isDirectory) {
          val localFiles = getArtifactsFromDir(libDir.getCanonicalPath, artifactType, Some(libDir.getName))
          acc ++ localFiles
        } else acc
      }
    }.toSeq
  }
}

class ArtifactServiceService extends ArtifactService
