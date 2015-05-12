package controllers

import com.tantalim.artifacts.compiler.TableCompiler
import com.tantalim.database.data.SqlBuilder
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

  def importArtifact(artifactType: String, name: String) = Action {
    timer("importArtifact") {
      try {
        val tableImport = new ArtifactImport(artifactType)
        val result = tableImport.readFromSourceAndWriteToDatabase(name)
        val message = if (result.state == DataState.Done) {
          Json.obj(
            "status" -> "success",
            "message" -> s"$artifactType($name) was imported"
          )
        } else {
          Json.obj(
            "status" -> "failure",
            "message" -> s"$artifactType($name) failed to import"
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

  def exportArtifact(artifactType: String, name: String) = Action {
    try {
      val tableExport = new ArtifactExport(artifactType)
      tableExport.readFromDatabaseAndWriteToSource(name)
      Ok(Json.obj(
        "status" -> "success",
        "message" -> s"$artifactType($name) was exported"
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