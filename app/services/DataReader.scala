package services

import java.sql.ResultSet

import data._
import models.{ModelParentLink, ModelOrderBy, ModelField, Model}
import play.api.libs.json._

trait DataReader extends ArtifactCompiler with Database {

  def queryOneRow(model: Model, id: TntValue): Option[SmartNodeInstance] = {
    queryOneRow(model, Some(f"${model.instanceID.get} = $id"))
  }

  def queryOneRow(model: Model, filter: Option[String]): Option[SmartNodeInstance] = {
    val existingRows = queryModelData(model, 1, filter, Seq.empty)
    existingRows.rows.headOption
  }

  def queryModelData(model: Model, page: Int, filter: Option[String], orderBy: Seq[ModelOrderBy]): SmartNodeSet = {
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
    val resultSet = convertResultSetToDataRows(model, rs)
    if (resultSet.rows.length > 0 && model.children.size > 0) {
      model.children.map {
        case (childModelName: String, childModel: Model) => {
          val parentIDs = getValueFromAllRows(resultSet.rows, childModel.parentLink.get.parentField)
          val filterForChildModel = childModel.parentLink.get.childField + " In " + parentIDs.mkString(",")
          val childRows = queryModelData(childModel, 1, Some(filterForChildModel), childModel.orderBy)
          addChildRowsToParent(resultSet, childRows)
        }
      }
    }
    resultSet
  }

  private def convertResultSetToDataRows(model: Model, rs: ResultSet) = {
    val resultBuilder = new SmartNodeSet(model)
    while (rs.next()) {
      val newInstance = resultBuilder.insert
      model.fields.foreach {
        case (fieldName, f) =>
          newInstance.data + fieldName -> {
            f.basisColumn.dataType match {
              case "Int" | "Integer" => TntInt(rs.getInt(fieldName))
              case "String" => TntString(rs.getString(fieldName))
              case "Boolean" => TntBoolean(rs.getBoolean(fieldName))
              case "Boolean" => TntDate(rs.getDate(fieldName))
              case _ => throw new MatchError(f"field.dataType of `${f.basisColumn.dataType}` is not String or Integer")
            }
          }
      }
      newInstance.id = if (model.instanceID.isDefined) newInstance.data.get(model.instanceID.get) else None
    }
    resultBuilder
  }

  private def getValueFromAllRows(dataRows: Seq[SmartNodeInstance], fieldName: String) = {
    dataRows.map { row =>
      row.data.get(fieldName)
    }
  }

  private def addChildRowsToParent(parentRows: SmartNodeSet,
                                   childRows: SmartNodeSet): Unit = {

    val parentLink = childRows.model.parentLink.get
    var remainingChildRows = childRows.rows
    parentRows.rows.foreach { parentRow =>
      val parentID = parentRow.get(parentLink.parentField).get
      val (matching, nonMatching) = remainingChildRows.partition { childRow =>
        parentID == childRow.get(parentLink.childField).get
      }
      remainingChildRows = nonMatching
      parentRow.children + (childRows.model.name -> matching)
    }
    if (remainingChildRows.length > 0) {
      throw new Exception(s"Failed to match ${remainingChildRows.length} ${childRows.model.name} records. Check datatypes of $parentLink")
    }

  }

}

class DataReaderService extends DataReader
