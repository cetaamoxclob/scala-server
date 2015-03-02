package controllers

import com.tantalim.util.{Timer, LoginStrategyType}
import data.{DataState, DataConverters, SmartNodeSet}
import com.tantalim.models.{ArtifactType, ModelOrderBy, User}
import play.api.libs.json._
import play.api.mvc._
import services._

object Application extends Controller with Timer {
  val compiler = new ArtifactCompilerService

  private val applicationMenu = "Default"

  def index = Action {
    val menu = compiler.compileMenu(applicationMenu)
    Redirect(menu.content.head.items.head.href)
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
      val menu = compiler.compileMenu(applicationMenu)
      val page = compiler.compilePage(name)
      val user = new User("12345", "trevorallred", "Trevor Allred")
      Ok(views.html.desktop.page.index(page, menu, user))
    }
  }

  def mobile(name: String) = TODO

  def importList = Action {
    implicit request =>
      val menu = compiler.compileMenu(applicationMenu)
      val artifactList = new ArtifactServiceService().findArtifacts
      val user = new User("12345", "trevorallred", "Trevor Allred")
      Ok(views.html.desktop.importList(menu, user, artifactList))
  }

  def importArtifact(artifactType: String, name: String) = Action {
    timer("importArtifact") {
      val tableImport = new ArtifactImport(ArtifactType.valueOf(artifactType))
      val result = tableImport.readFromSourceAndWriteToDatabase(name)
      val message = if (result.state == DataState.Done) {
        "success" -> s"$artifactType($name) was imported"
      } else {
        "failure" -> s"$artifactType($name) failed to import"
      }
      Redirect(controllers.routes.Application.importList()).flashing(message)
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