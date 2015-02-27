package services

import java.sql.ResultSet

import data._
import models.{Model, ModelOrderBy}

trait DataReader extends ArtifactCompiler with Database {

  //  def queryOneRow(model: Model, id: TntValue): Option[SmartNodeInstance] = {
  //    queryOneRow(model, Some(f"${model.instanceID.get} = $id"))
  //  }
  //
  //  def queryOneRow(model: Model, filter: Option[String]): Option[SmartNodeInstance] = {
  //    val existingRows = queryModelData(model, 1, filter, Seq.empty)
  //    existingRows.rows.headOption
  //  }

  def queryModelData(model: Model, page: Int = 1, filter: Option[String] = None, orderBy: Seq[ModelOrderBy] = Seq.empty): SmartNodeSet = {
    var sqlBuilder = new SqlBuilder(
      from = model.basisTable.dbName,
      fields = model.fields,
      page = page,
      orderBy = orderBy,
      limit = model.limit)

    if (filter.isDefined) {
      DataFilter.parse(filter.get, model.fields) match {
        case (where: String, params: List[Any]) => if (!params.isEmpty) {
          sqlBuilder = sqlBuilder.copy(
            where = Some(where),
            parameters = params
          )
        }
      }
    }

    val rs = query(sqlBuilder.toPreparedStatement, sqlBuilder.parameters)
    val resultSet = convertResultSetToDataRows(model, rs)
    if (resultSet.rows.length > 0 && model.children.size > 0) {
      model.children.map {
        case (childModelName: String, childModel: Model) =>
          val parentFieldName = childModel.parentLink.get.parentField
          val parentIDs: Seq[Int] = resultSet.rows.map { row =>
            row.get(parentFieldName) match {
              case Some(parentFieldValue) => parentFieldValue.asInstanceOf[TntInt].value.toInt
              case None => -1
            }
          }
          val filterForChildModel = childModel.parentLink.get.childField + " In " + parentIDs.mkString(",")
          val childRows = queryModelData(childModel, 1, Some(filterForChildModel), childModel.orderBy)
          addChildRowsToParent(resultSet, childRows)
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
          newInstance.set(fieldName, {
            f.basisColumn.dataType match {
              case "Int" | "Integer" => TntInt(rs.getInt(fieldName))
              case "String" => TntString(rs.getString(fieldName))
              case "Boolean" => TntBoolean(rs.getBoolean(fieldName))
              case "Date" => {
                rs.getDate(fieldName) match {
                  case null => TntNull()
                  case _ => TntDate(rs.getDate(fieldName))
                }
              }
              case _ => throw new MatchError(f"field.dataType of `${f.basisColumn.dataType}` is not String or Integer")
            }
          })
      }
      newInstance.id = if (model.instanceID.isDefined) newInstance.data.get(model.instanceID.get) else None
    }
    resultBuilder
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
      val childSet = new SmartNodeSet(childRows.model, parentInstance = Some(parentRow))
      childSet.rows ++= matching
      parentRow.children += (childRows.model.name -> childSet)
    }
    if (remainingChildRows.length > 0) {
      throw new Exception(s"Failed to match ${remainingChildRows.length} ${childRows.model.name} records. Check datatypes of $parentLink")
    }

  }

}

class DataReaderService extends DataReader
