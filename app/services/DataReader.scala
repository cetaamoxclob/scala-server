package services

import java.sql.ResultSet

import data.{SqlBuilder, DataFilter, Database}
import models.{ModelOrderBy, ModelField, Model}
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

  def queryOneRow(model: Model, id: Int): SelectDataRow = {
    queryOneRow(model, Some(f"${model.instanceID.get} = $id"))
  }

  def queryOneRow(model: Model, filter: Option[String]): SelectDataRow = {
    val existingRows = queryModelData(model, 1, filter, Seq.empty)
    if (existingRows.size != 1) {
      throw new Exception(f"Failed to find exactly 1 record for ${model.name} where ${filter}")
    }
    existingRows.head
  }

  def queryModelData(model: Model, page: Int, filter: Option[String], orderBy: Seq[ModelOrderBy]): Seq[SelectDataRow] = {
    var sqlBuilder = new SqlBuilder(
      from = model.basisTable.dbName,
      fields = model.fields,
      page = page,
      orderBy = orderBy,
      limit = model.limit)

    if (filter.isDefined) {
      val parseResults = DataFilter.parse(filter.get, model.fields)
      sqlBuilder = sqlBuilder.copy(
        where = Option(parseResults._1),
        parameters = parseResults._2
      )
    }

    val rs = query(sqlBuilder.toPreparedStatement, sqlBuilder.parameters)
    val dataRows = convertResultSetToDataRows(model.instanceID, model.fields, rs)
    if (dataRows.length > 0 && model.children.size > 0) {
      model.children.map { childModelResult =>
        println(s"Get child data for ${childModelResult._1}")
        val childModel = childModelResult._2
        val parentIDs = getValues(dataRows, childModel.parentLink.get.parentField)
        val childFilter = childModel.parentLink.get.childField + " In " + parentIDs.mkString(",")
        queryModelData(childModel, 1, Some(childFilter), childModel.orderBy)
      }
      dataRows
    } else dataRows
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
              case "Boolean" => JsBoolean(rs.getBoolean(fieldName))
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

  private def getValues(dataRows: Seq[SelectDataRow], fieldName: String) = {
    dataRows.map{ row =>
      row.data(fieldName)
    }
  }

}

class DataReaderService extends DataReader
