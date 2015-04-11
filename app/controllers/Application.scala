package controllers

import _root_.core.compiler.{MenuCompiler, PageCompiler, TableCompiler}
import _root_.core.data.SqlBuilder
import com.tantalim.models.{ArtifactType, User}
import com.tantalim.nodes.DataState
import com.tantalim.util.{LoginStrategyType, TantalimException, Timer}
import play.api.libs.json._
import play.api.mvc._
import services._

object Application extends Controller with Timer {

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
          "message" -> e.getMessage,
          "help" -> e.getHelp
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