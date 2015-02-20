package services

import data.Database
import models.Model
import play.api.libs.json._
import play.api.libs.functional.syntax._

case class SelectDataRow(id: Option[String],
                         data: Map[String, JsValue],
                         children: Option[Map[String, SelectDataRow]])

object SelectDataRow {
  implicit def selectDataWrites: Writes[SelectDataRow] = (
    (JsPath \ "id").writeNullable[String] and
      (JsPath \ "data").write[Map[String, JsValue]] and
      (JsPath \ "children").lazyWriteNullable(Writes.map[SelectDataRow](selectDataWrites))
    )(unlift(SelectDataRow.unapply))
}

trait DataReader extends ArtifactCompiler with Database {

  def getData(modelName: String, page: Int, filter: Option[String]): Seq[SelectDataRow] = {
    val model = compileModel(modelName)
    queryModelData(model, page, filter)
  }

  def queryModelData(model: Model, page: Int, filter: Option[String]): Seq[SelectDataRow] = {
    val sqlString = convertModelToSql(model)
    val rs = query(sqlString)
    val resultBuilder = Seq.newBuilder[SelectDataRow]

    while (rs.next()) {
      resultBuilder += new SelectDataRow(
        id = if (model.instanceID.isDefined) Some(rs.getString(model.instanceID.get)) else None,
        data = model.fields.map {
          case (fieldName, f) => {
            val columnValue: JsValue = f.dataType match {
              case "Int" | "Integer" => JsNumber(rs.getInt(fieldName))
              case "String" => JsString(rs.getString(fieldName))
              case _ => throw new MatchError(f"field.dataType of `${f.dataType}` is not String or Integer")
            }
            fieldName -> columnValue
          }
        },
        children = None
      )
    }
    resultBuilder.result
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
}

class DataReaderService extends DataReader
