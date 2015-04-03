package controllers

import com.tantalim.nodes.{DataState, SmartNodeSet}
import com.tantalim.util.{TantalimException, Timer, LoginStrategyType}
import compiler.{MenuCompiler, TableCompiler, ModelCompiler, PageCompiler}
import data.{SqlBuilder, DataConverters}
import com.tantalim.models.{ArtifactType, ModelOrderBy, User}
import play.api.libs.json._
import play.api.mvc._
import services._

object Application extends Controller with Timer {
  val menuCompiler = new MenuCompiler {}

  private val applicationMenu = "Default"

  def index = Action {
    val menu = menuCompiler.compileMenu(applicationMenu)
    Redirect(menu.content.head.items.head.href)
  }

  def readData(name: String, page: Int = 1, filter: Option[String] = None, orderBy: Option[ModelOrderBy] = None) = Action {
    timer("readData") {
      try {
        val reader = new DataReader {}
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
        val saver = new DataSaverService
        saver.saveAll(dataSet)
        val jsonResponse = DataConverters.convertSmartNodeSetToJsonArr(dataSet)
        Ok(jsonResponse)
      } catch {
        case e: TantalimException => Ok(convertExceptionToJson(e))
        case e: Exception => Ok(convertExceptionToJson(e))
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

  def mobile(name: String) = TODO

  def ddl(tableName: String) = Action {
    val tableSchema = new TableSchema {}
    try {
      val tableCompiler = new TableCompiler {}
      val table = tableCompiler.compileTable(tableName)
      tableSchema.drop(table)
      tableSchema.create(table)
      Ok(Json.obj(
        "status" -> "success",
        "message" -> s"${SqlBuilder.getTableSql(table)} was (re)created"
      ))
    } catch {
      case e: Exception => Ok(Json.obj(
        "status" -> "failure",
        "message" -> e.getMessage
      ))
      case e: TantalimException => Ok(Json.obj(
        "status" -> "failure",
        "message" -> e.getMessage
      ))
    }
  }

  def artifactList = Action {
    val artifactList = new ArtifactLister {}.getArtifactList
    Ok(artifactList)
  }

  def importArtifact(module: String, artifactType: String, name: String) = Action {
    timer("importArtifact") {
      try {
        val tableImport = new ArtifactImport(ArtifactType.valueOf(artifactType))
        val result = tableImport.readFromSourceAndWriteToDatabase(module, name)
        val message = if (result.state == DataState.Done) {
          Json.obj(
            "status" -> "success",
            "message" -> s"$artifactType($module.$name) was imported"
          )
        } else {
          Json.obj(
            "status" -> "failure",
            "message" -> s"$artifactType($module.$name) failed to import"
          )
        }
        Ok(message)

      } catch {
        case e: TantalimException => Ok(Json.obj(
          "status" -> "failure",
          "message" -> e.getMessage
        ))
        case e: Exception => Ok(Json.obj(
          "status" -> "failure",
          "message" -> e.getMessage
        ))
        case e => Ok(Json.obj(
          "status" -> "failure",
          "message" -> e.toString
        ))
      }
    }
  }

  def exportArtifact(module: String, artifactType: String, name: String) = Action {
    try {
      val tableExport = new ArtifactExport(ArtifactType.valueOf(artifactType))
      tableExport.readFromDatabaseAndWriteToSource(module, name)
      Ok(Json.obj(
        "status" -> "success",
        "message" -> s"$artifactType($module.$name) was exported"
      ))
    } catch {
      case e: Exception => Ok(Json.obj(
        "status" -> "failure",
        "message" -> e.getMessage
      ))
      case e: TantalimException => Ok(Json.obj(
        "status" -> "failure",
        "message" -> e.getMessage
      ))
      case _ => Ok("Unknown error")
    }
  }

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