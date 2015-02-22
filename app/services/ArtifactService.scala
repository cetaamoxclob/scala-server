package services

import com.google.common.base.Charsets
import com.google.common.io.Files
import models.src.{TableJson, ModelJson, PageJson}
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

  def getMenu: JsResult[Menu] = {
    getMenu("Default")
  }

  def getMenu(name: String): JsResult[Menu] = {
    val artifactJson = getArtifactContentAndParseJson(ArtifactType.Menu, name)
    artifactJson.validate[Menu]
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
    val artifactJson = getArtifactContentAndParseJson(ArtifactType.Model, name)
    artifactJson.validate[TableJson]
  }

}
