package services

import com.google.common.base.Charsets
import com.google.common.io.Files
import play.api.Play.current
import models._
import play.api.Play
import play.api.libs.json.{JsValue, JsError, JsSuccess, Json}

object ArtifactService {

  def getArtifactContentAndParseJson(artifactType: ArtifactType, name: String): JsValue = {
    val directoryName = "src/" + artifactType.getDirectory + "/" + name + ".json"
    val artifactContent = Files.toString(Play.getFile(directoryName), Charsets.UTF_8)
    Json.parse(artifactContent)
  }

  def getMenu: Menu = {
    getMenu("Default")
  }

  def getMenu(name: String): Menu = {
    val artifactJson = getArtifactContentAndParseJson(ArtifactType.Menu, name)
    artifactJson.validate[Menu] match {
      case s: JsSuccess[Menu] => {
        s.get
      }
      case e: JsError => {
        val menu = new Menu(
          "title", ???
        )
        menu
      }
    }
  }

  def getPage(name: String): Page = {
    val artifactJson = getArtifactContentAndParseJson(ArtifactType.Page, name)
    artifactJson.validate[PageJson] match {
      case s: JsSuccess[PageJson] => {
        val pageJson = s.get
        Page.compile(name, pageJson)
      }
      case e: JsError => {
        println(e)
        val menu = new Page(
          "Error", "Error", None, None
        )
        menu
      }
    }
  }

  def getModel(name: String): Model = {
    val model = new Model(name)
    model
  }

  def getTable(name: String): Table = {
    val table = new Table(name)
    table
  }

}
