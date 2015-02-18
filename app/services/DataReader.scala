package services

import java.sql.ResultSet

import models.Model
import play.api.Play.current
import play.api.db.DB
import play.api.libs.json.Json._
import play.api.libs.json._

class DataReader extends ArtifactCompiler {

  def getData(modelName: String, page: Int, filter: Option[String]): JsValue = {
    try {
      val model = compileModel(modelName)
      val jsArray = queryModelData(model)
      toJson(jsArray)
    } catch {
      case e: Exception =>
        toJson(Map("error" -> (e.toString)))
    }
  }

  def queryModelData(model: Model): JsArray = {
    val conn = DB.getConnection()
    val stmt = conn.createStatement
    try {
      val sqlString = convertModelToSql(model)
      val rs = stmt.executeQuery(sqlString)
      appendResult(model, rs, JsArray())
    } finally {
      stmt.close
      conn.close
    }
  }

  def convertModelToSql(model: Model): String = {
    val sql = new StringBuilder
    sql append "SELECT "
    sql append model.fields.map {
      case (fieldName, f) => {
        f.dbName + " AS " + fieldName
      }
    }.toSeq.mkString(", ")
    sql append " FROM `" append model.basisTable.dbName append "` "
    sql.toString
  }

  private def appendResult(model: Model, rs: ResultSet, allRows: JsArray): JsArray = {
    if (rs.next()) {
      val data = model.fields.map {
        case (fieldName, f) => {
          val columnValue: JsValue = f.dataType match {
            case "Int" => JsNumber(rs.getInt(fieldName))
            case "String" => JsString(rs.getString(fieldName))
          }
          fieldName -> columnValue
        }
      }.toSeq

      val instanceID = if (model.instanceID.isDefined) {
        rs.getString(model.instanceID.get)
      } else {
        "asdf"
      }
      val row = JsObject(Seq(
        "id" -> JsString(instanceID),
        "data" -> JsObject(data)
      ))
      appendResult(model, rs, allRows :+ row)
    } else {
      allRows
    }
  }


}
