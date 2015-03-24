package controllers

import com.tantalim.nodes.{DataState, SmartNodeSet}
import com.tantalim.util.{TantalimException, Timer, LoginStrategyType}
import compiler.{MenuCompiler, TableCompiler, ModelCompiler, PageCompiler}
import data.DataConverters
import com.tantalim.models.{PageSection, ArtifactType, ModelOrderBy, User}
import play.api.libs.json._
import play.api.mvc._
import services._

object Application extends Controller with Timer {
  val compiler = new PageCompiler with ModelCompiler with TableCompiler with MenuCompiler

  private val applicationMenu = "Default"

  def index = Action {
    val menu = compiler.compileMenu(applicationMenu)
    Redirect(menu.content.head.items.head.href)
  }

  def readData(name: String, page: Int = 1, filter: Option[String] = None, orderBy: Option[ModelOrderBy] = None) = Action {
    timer("readData") {
      try {
        val model = compiler.compileModel(name)
        val reader = new DataReader {}
        val smartSet: SmartNodeSet = reader.queryModelData(model, page, filter, if (orderBy.isDefined) Seq(orderBy.get) else model.orderBy)
        val totalPages = reader.calcTotalRows(model, filter)
        Ok(Json.obj(
          "maxPages" -> JsNumber(totalPages),
          //          "sql" -> JsString("SELECT FROM ..."),
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
        val model = compiler.compileModel(name)
        val dataSet = new SmartNodeSet(model)
        DataConverters.convertJsArrayToSmartNodeSet(dataSet, request.body.as[JsArray])
        val saver = new DataSaverService
        saver.saveAll(dataSet)
        val jsonResponse = DataConverters.convertSmartNodeSetToJsonArr(dataSet)
        Ok(jsonResponse)
      } catch {
        case e: Exception => Ok(convertExceptionToJson(e))
      }
    }
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
        val menu = compiler.compileMenu(applicationMenu)
        val page = compiler.compilePage(name)
        val user = new User("12345", "trevorallred", "Trevor Allred")
        Ok(views.html.desktop.page.index(page, menu, user))
      } catch {
        case e: TantalimException => Ok(views.html.error(e))
      }
    }
  }

  def section(pageName: String, sectionName: String) = Action {

    def findSection(haystack: Seq[PageSection], needle: String): Option[PageSection] = {
      val found = haystack.find(p => p.name == needle)
      if (found.isDefined) found
      else {
        haystack.flatMap(p => findSection(p.sections, needle)).headOption
      }
    }
    def getNames(haystack: Seq[PageSection]): Seq[String] = {
      val names = haystack.map(_.name).toSeq
      val childNames = haystack.flatMap(p => getNames(p.sections))
      names ++ childNames
    }

    timer("section") {
      try {
        val page = compiler.compilePage(pageName)
        val foundChildren = findSection(page.sections, sectionName)
        val section = if (foundChildren.headOption.isDefined) foundChildren.head
        else {
          val pageSections = getNames(page.sections).mkString(",")
          throw new TantalimException(s"Failed to find Section named $sectionName", s"Use one of the following sections: $pageSections")
        }
        Ok(views.html.desktop.page.section(section, 0))
      } catch {
        case e: TantalimException => Ok(views.html.error(e))
      }
    }
  }

  def mobile(name: String) = TODO

  def ddl(tableName: String) = Action {
      val table = compiler.compileTable(tableName)
      val tableSchema = new TableSchema {}
      Ok(tableSchema.generateTableDDL(table))
  }

//  def ddl_run(tableName: String) = Action {
//  }

  def importList = Action {
    implicit request =>
      val menu = compiler.compileMenu(applicationMenu)
      val artifactList = new ArtifactServiceService().findArtifacts
      val user = new User("12345", "trevorallred", "Trevor Allred")
      Ok(views.html.desktop.importList(menu, user, artifactList))
  }

  def importArtifact(artifactType: String, name: String) = Action {
    timer("importArtifact") {
      try {
        val tableImport = new ArtifactImport(ArtifactType.valueOf(artifactType))
        val result = tableImport.readFromSourceAndWriteToDatabase(name)
        val message = if (result.state == DataState.Done) {
          "success" -> s"$artifactType($name) was imported"
        } else {
          "failure" -> s"$artifactType($name) failed to import"
        }
        Redirect(controllers.routes.Application.importList()).flashing(message)
      } catch {
        case e: TantalimException => Ok(views.html.error(e))
      }
    }
  }

  def exportArtifact(artifactType: String, name: String) = Action {
    val tableExport = new ArtifactExport(ArtifactType.valueOf(artifactType))
    val output = tableExport.readFromDatabaseAndWriteToSource(name)
    Ok(Json.prettyPrint(output))
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