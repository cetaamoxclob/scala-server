package services

import java.sql.ResultSet

import data.{SqlBuilder, DataFilter, Database}
import models.{ModelField, Model}
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

  def queryOneRow(model: Model, id: Int): SelectDataRow = {
    queryOneRow(model, Some(f"${model.instanceID.get} = $id"))
  }

  def queryOneRow(model: Model, filter: Option[String]): SelectDataRow = {
    val existingRows = queryModelData(model, 1, filter)
    if (existingRows.size != 1) {
      throw new Exception(f"Failed to find exactly 1 record for ${model.name} where ${filter}")
    }
    existingRows.head
  }

  def queryModelData(model: Model, page: Int, filter: Option[String]): Seq[SelectDataRow] = {
    var sqlBuilder = new SqlBuilder(
      from = model.basisTable.dbName,
      fields = model.fields,
      page = page,
      limit = model.limit)

    if (filter.isDefined) {
      val parseResults = DataFilter.parse(filter.get, model.fields)
      sqlBuilder = sqlBuilder.copy(
        where = Option(parseResults._1),
        parameters = parseResults._2
      )
    }

    val rs = query(sqlBuilder.toPreparedStatement, sqlBuilder.parameters)
    convertResultSetToDataRows(model.instanceID, model.fields, rs)
  }

  private def convertResultSetToDataRows(instanceID: Option[String], fields: Map[String, ModelField], rs: ResultSet) = {
    val resultBuilder = Seq.newBuilder[SelectDataRow]

    while (rs.next()) {
      resultBuilder += new SelectDataRow(
        id = if (instanceID.isDefined) Some(rs.getString(instanceID.get)) else None,
        data = fields.map {
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
}

class DataReaderService extends DataReader
