package controllers

import models.User
import play.api.libs.json.Json
import play.api.mvc._
import services._

object Application extends Controller with util.Timer {
  val compiler = new ArtifactCompilerService

  def index = {
    desktop("ListTables") // TODO Get the default from the menu
  }

  def readData(name: String, page: Int, filter: Option[String]) = Action {
    val reader = new DataReaderService
    val response: Seq[SelectDataRow] = reader.getData(name, page, filter)
    Ok(Json.toJson(response))
  }

  def saveData(name: String) = Action { request =>
    val saver = new DataSaver
    val model = compiler.compileModel(name)
    val response = saver.saveAll(model, request.body.asJson)
    Ok(response)
  }

  def desktop(name: String) = Action {
    timer("desktop") {
      val menu = compiler.compileMenu("Default")
      val page = compiler.compilePage(name)
      val user = new User("12345", "trevorallred", "Trevor Allred")
      Ok(views.html.desktop.index(page, menu, user))
    }
  }

  def mobile(name: String) = TODO

  def importAll = TODO

  def exportAll = TODO

  def login = TODO

  def logout = Action {
    Redirect("/")
  }

}