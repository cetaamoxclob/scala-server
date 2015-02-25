package controllers

import tantalim.util.{Timer, LoginStrategyType}
import data.{DataConverters, SmartNodeSet}
import models.{ArtifactType, ModelOrderBy, User}
import play.api.libs.json._
import play.api.mvc._
import services._

object Application extends Controller with Timer {
  val compiler = new ArtifactCompilerService

  def index = {
    desktop("ListTables") // TODO Get the default from the menu
  }

  def readData(name: String, page: Int = 1, filter: Option[String] = None, orderBy: Option[ModelOrderBy] = None) = Action {
    timer("readData") {
      val model = compiler.compileModel(name)
      val reader = new DataReaderService
      val smartSet: SmartNodeSet = reader.queryModelData(model, page, filter, if (orderBy.isDefined) Seq(orderBy.get) else model.orderBy)
      Ok(DataConverters.convertSmartNodeSetToJsonArr(smartSet))
    }
  }

  def saveData(name: String) = Action(parse.json) { request =>
    timer("saveData") {
      val model = compiler.compileModel(name)
      val dataSet = new SmartNodeSet(model)
      DataConverters.convertJsArrayToSmartNodeSet(dataSet, request.body.as[JsArray])
      val saver = new DataSaverService
      saver.saveAll(dataSet)
      val jsonResponse = DataConverters.convertSmartNodeSetToJsonArr(dataSet)
      Ok(jsonResponse)
    }
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
    timer("importAll") {
      val tableImport = new ArtifactImport(ArtifactType.Table)
      val output = tableImport.readFromSourceAndWriteToDatabase("Column")
      Ok(Json.arr())
    }
  }

  def exportAll = TODO

  def login = Action {
    timer("login") {
      val message = None // Some("Message here")
      val loginStrategies = List(LoginStrategyType.Github)
      Ok(views.html.login("Scala Test", message, loginStrategies))
    }
  }

  def logout = Action {
    Redirect("/login")
  }

  def auth(strategy: String) = Action {
    Redirect("/")
  }

}