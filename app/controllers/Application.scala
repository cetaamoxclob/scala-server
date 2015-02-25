package controllers

import data.{DataConverters, SmartNodeSet}
import models.{ArtifactType, ModelOrderBy, User}
import play.api.libs.json.Json
import play.api.mvc._
import services._
import util.LoginStrategyType

import scala.util.Success

object Application extends Controller with util.Timer {
  val compiler = new ArtifactCompilerService

  def index = {
    desktop("ListTables") // TODO Get the default from the menu
  }

  def readData(name: String, page: Int, filter: Option[String], orderBy: Option[ModelOrderBy]) = Action {
    val model = compiler.compileModel(name)
    val reader = new DataReaderService
    val smartSet: SmartNodeSet = reader.queryModelData(model, page, filter, if (orderBy.isDefined) Seq(orderBy.get) else model.orderBy)
    Ok(DataConverters.convertSmartNodeSetToJsonArr(smartSet))
  }

  def saveData(name: String) = Action { request =>
    val saver = new DataSaverService
    val model = compiler.compileModel(name)
    val dataSet: SmartNodeSet = ??? // Json.fromJson(request.body.asJson.get)
    saver.saveAll(dataSet)
    val jsonResponse = Json.arr() // TODO dataSet
    Ok(jsonResponse)
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

  def importAll = Action {
    val tableImport = new ArtifactImport(ArtifactType.Table)
    val output = tableImport.readFromSourceAndWriteToDatabase("Column")
    Ok(Json.arr())
  }

  def exportAll = TODO

  def login = Action {
    val message = None // Some("Message here")
    val loginStrategies = List(LoginStrategyType.Github)
    Ok(views.html.login("Scala Test", message, loginStrategies))
  }

  def logout = Action {
    Redirect("/login")
  }

  def auth(strategy: String) = Action {
    Redirect("/")
  }

}