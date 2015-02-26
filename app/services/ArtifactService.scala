package services

import java.io.File

import com.google.common.base.Charsets
import com.google.common.io.Files
import models.src._
import play.api.Play.current
import models._
import play.api.Play
import play.api.libs.json._

trait ArtifactService {

  def getArtifactContentAndParseJson(artifactType: ArtifactType, name: String): JsValue = {
    val directoryName = "src/" + artifactType.getDirectory + "/" + name + ".json"
    val artifactContent = Files.toString(Play.getFile(directoryName), Charsets.UTF_8)

    Json.parse(artifactContent)
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

  def findArtifacts : Seq[ArtifactStub] = {

    def artifactName(rootPath: String, filePath: String): String = {
//      rootPath + "-" + filePath
      filePath.replace(rootPath, "").replace(".json", "").replace("/", "")
    }

    ArtifactType.values().flatMap{ artifactType: ArtifactType =>
      val artifactDir = new File("src/" + artifactType.getDirectory)
      val allFiles = artifactDir.listFiles()
      val filesList = allFiles.find(f => f.isFile())
      val temp = allFiles.map(f => ArtifactStub(artifactType, artifactName(artifactDir.getAbsolutePath, f.getCanonicalPath)))
      temp
    }.toSeq
  }
}

class ArtifactServiceService extends ArtifactService
