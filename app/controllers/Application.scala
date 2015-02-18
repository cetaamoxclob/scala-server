package controllers

import models.User
import play.api.mvc._
import services.{ArtifactCompilerService, DataReader, ArtifactCompiler}

object Application extends Controller {

  def index = {
    desktop("ListTables") // TODO Get the default from the menu
  }

  def readData(name: String, page: Int, filter: Option[String]) = Action { request =>
    val reader = new DataReader
    val response = reader.getData(name, page, filter)
    Ok(response)
  }

  def desktop(name: String) = Action {
    val compiler = new ArtifactCompilerService
    val menu = compiler.compileMenu("Default")
    val page = compiler.compilePage(name)
    val user = new User("12345", "trevorallred", "Trevor Allred")
    Ok(views.html.desktop.index(page, menu, user))
  }

}