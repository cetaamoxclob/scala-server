package controllers

import models.User
import play.api.mvc._
import services.{ArtifactCompiler, ArtifactService, DataReader}

object Application extends Controller {

  def index = {
    desktop("Home")
  }

  def readData(name: String) = Action { request =>
    val dataReader = new DataReader
    val response = dataReader.read(name)
    Ok(response)
  }

  def desktop(name: String) = Action {
    val menu = ArtifactCompiler.compileMenu("Default")
    val page = ArtifactCompiler.compilePage(name)
    val user = new User("12345", "trevorallred", "Trevor Allred")
    Ok(views.html.desktop.index(page, menu, user))
  }

}