package controllers.core

import com.tantalim.models.{User, ModelOrderBy}
import com.tantalim.nodes.SmartNodeSet
import com.tantalim.util.{Timer, TantalimException}
import com.tantalim.artifacts.compiler.{MenuCompiler, PageCompiler, ModelCompiler}
import play.api.libs.json.{JsArray, JsString, JsNumber, Json}
import play.api.mvc._
import com.tantalim.database.services.{DataSaver, DataReader}

object Application extends Controller with Timer {
  val menuCompiler = new MenuCompiler {}

  private val applicationMenu = "Default"

  def index = Action {
    val menu = menuCompiler.compileMenu(applicationMenu)
    Redirect(menu.content.head.items.head.href)
  }

  def desktop(name: String) = Action {
    timer("desktop") {
      try {
        val menu = menuCompiler.compileMenu(applicationMenu)
        val pageCompiler = new PageCompiler {}
        val page = pageCompiler.compilePage(name)
        val user = new User("12345", "trevorallred", "Trevor Allred")
        Ok(views.html.desktop.page.index(page, menu, user))
      } catch {
        case e: TantalimException => Ok(views.html.error(e))
      }
    }
  }


  private def compileModel(name: String) = {
    val modelCompiler = new ModelCompiler {}
    modelCompiler.compileModel(name)
  }

  private def convertExceptionToJson(e: Exception) = {
    Json.obj(
      "error" -> Json.obj(
        "message" -> e.getMessage
      )
    )
  }

  private def convertExceptionToJson(e: TantalimException) = {
    Json.obj(
      "error" -> Json.obj(
        "message" -> e.getMessage,
        "help" -> e.getHelp
      )
    )
  }

  def readData(name: String, page: Int = 1, filter: Option[String] = None, orderBy: Option[ModelOrderBy] = None) = Action {
    timer("readData") {
      try {
        val reader = new DataReader with PlayableDatabaseConnection
        val model = compileModel(name)
        val smartSet: SmartNodeSet = reader.queryModelData(model, page, filter, if (orderBy.isDefined) Seq(orderBy.get) else model.orderBy)
        val totalPages = reader.calcTotalRows(model, filter)
        Ok(Json.obj(
          "maxPages" -> JsNumber(totalPages),
          "sql" -> JsString(smartSet.sql),
          "rows" -> DataConverters.convertSmartNodeSetToJsonArr(smartSet)
        ))
      } catch {
        case e: TantalimException => Ok(convertExceptionToJson(e))
        case e: Exception => Ok(convertExceptionToJson(e))
      }
    }
  }

  def saveData(name: String) = Action(parse.json) { request =>
    timer("saveData") {
      try {
        val model = compileModel(name)
        val dataSet = new SmartNodeSet(model)
        DataConverters.convertJsArrayToSmartNodeSet(dataSet, request.body.as[JsArray])
        val saver = new DataSaver with PlayableDatabaseConnection
        saver.saveAll(dataSet)
        val jsonResponse = DataConverters.convertSmartNodeSetToJsonArr(dataSet)
        Ok(jsonResponse)
      } catch {
        case e: TantalimException => Ok(convertExceptionToJson(e))
        case e: Exception => Ok(convertExceptionToJson(e))
      }
    }
  }

  def mobile(name: String) = TODO


}
