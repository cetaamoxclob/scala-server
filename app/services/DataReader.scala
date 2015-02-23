package services

import java.sql.ResultSet

import data._
import models.{ModelParentLink, ModelOrderBy, ModelField, Model}
import play.api.libs.json._

trait DataReader extends ArtifactCompiler with Database {

  def queryOneRow(model: Model, id: Int): Option[DataInstance] = {
    queryOneRow(model, Some(f"${model.instanceID.get} = $id"))
  }

  def queryOneRow(model: Model, filter: Option[String]): Option[DataInstance] = {
    val existingRows = queryModelData(model, 1, filter, Seq.empty)
    if (existingRows.size != 1) {
      throw new Exception(f"Failed to find exactly 1 record for ${model.name} where ${filter}")
    }
    existingRows.headOption
  }

  def queryModelData(model: Model, page: Int, filter: Option[String], orderBy: Seq[ModelOrderBy]): Seq[DataInstance] = {
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
        val childModel = childModelResult._2
        val parentIDs = getValues(dataRows, childModel.parentLink.get.parentField)
        val childFilter = childModel.parentLink.get.childField + " In " + parentIDs.mkString(",")
        val childRows = queryModelData(childModel, 1, Some(childFilter), childModel.orderBy)
        addChildRowsToParent(dataRows, childModel.name, childModel.parentLink.get, childRows)
      }
      dataRows
    } else dataRows
  }

  private def convertResultSetToDataRows(instanceID: Option[String], fields: Map[String, ModelField], rs: ResultSet) = {
    val resultBuilder = Seq.newBuilder[DataInstance]

    while (rs.next()) {
      val fieldResults = fields.map {
        case (fieldName, f) => {
          val columnValue: JsValue = f.basisColumn.dataType match {
            case "Int" | "Integer" => JsNumber(rs.getInt(fieldName))
            case "String" => JsString(rs.getString(fieldName))
            case "Boolean" => JsBoolean(rs.getBoolean(fieldName))
            case _ => throw new MatchError(f"field.dataType of `${f.basisColumn.dataType}` is not String or Integer")
          }
          fieldName -> columnValue
        }
      }
      resultBuilder += new DataInstance(
        id = if (instanceID.isDefined) Some(fieldResults(instanceID.get).toString) else None,
        data = Some(fieldResults),
        children = None
      )
    }
    resultBuilder.result

  }

  private def getValues(dataRows: Seq[DataInstance], fieldName: String) = {
    dataRows.map { row =>
      row.data.get(fieldName)
    }
  }

  private def addChildRowsToParent(
                                    parentRows: Seq[DataInstance],
                                    childModelName: String,
                                    parentLink: ModelParentLink,
                                    childRows: Seq[DataInstance]): Unit = {

    var remainingChildRows = childRows
    parentRows.foreach { parentRow =>
      val parentID = parentRow.data.get.get(parentLink.parentField).get
      val (matching, nonMatching) = remainingChildRows.partition { childRow =>
        parentID == childRow.data.get.get(parentLink.childField).get
      }
      remainingChildRows = nonMatching
      parentRow.children = Some(parentRow.children.getOrElse(Map.empty) + (childModelName -> matching))
    }
    if (remainingChildRows.length > 0) {
      throw new Exception(s"Failed to match ${remainingChildRows.length} $childModelName records. Check datatypes of $parentLink")
    }

  }

}

class DataReaderService extends DataReader
