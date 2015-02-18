package controllers

import models.User
import play.api.mvc._
import services.{DataReader, ArtifactCompiler}

object Application extends Controller {

  def index = {
    desktop("Home")
  }

  def readData(name: String, page: Int, filter: Option[String]) = Action { request =>
    val response = DataReader.getData(name, page, filter)
    Ok(response)
  }

  def desktop(name: String) = Action {
    val menu = ArtifactCompiler.compileMenu("Default")
    val page = ArtifactCompiler.compilePage(name)
    val user = new User("12345", "trevorallred", "Trevor Allred")
    Ok(views.html.desktop.index(page, menu, user))
  }

}