package com.tantalim.database.services

import java.sql.{Connection, ResultSet}

import com.tantalim.filter.compiler.CompiledFilter
import com.tantalim.models._
import com.tantalim.nodes._
import com.tantalim.util.TantalimException
import com.tantalim.database.data.{DatabaseConnection, SqlBuilder}

trait DataReader extends DatabaseConnection {

  def calcTotalRows(model: Model, filter: Option[String] = None): Long = {
    if (model.limit == 0) return 1
    var sqlBuilder = new SqlBuilder(
      from = model.basisTable,
      steps = model.steps,
      fields = model.fields,
      limit = model.limit)
    sqlBuilder = parseFilterForSql(sqlBuilder, model.fields, filter)
    val rs = query(sqlBuilder.toCalcRowsStatement, sqlBuilder.parameters)
    rs.next()
    val rows = rs.getLong(1)
    calcMaxPages(rows, model.limit)
  }

  private def calcMaxPages(rows: Long, limit: Int) = Math.ceil(rows.toDouble / limit).toInt

  def queryModelData(model: Model, page: Int = 1, filter: Option[String] = None, orderBy: Seq[ModelOrderBy] = Seq.empty, dbConnection: Option[Connection] = None): SmartNodeSet = {
    if (model.basisTable.isMock) {
      throw new TantalimException(s"Model ${model.name} is based on a Mock Table and cannot query the database.", "Check the calling function.")
    }

    var sqlBuilder = new SqlBuilder(
      from = model.basisTable,
      steps = model.steps,
      fields = model.fields,
      page = page,
      orderBy = orderBy,
      limit = model.limit)

    sqlBuilder = parseFilterForSql(sqlBuilder, model.fields, model.filter)
    sqlBuilder = parseFilterForSql(sqlBuilder, model.fields, filter)

    try {
      val rs = if (dbConnection.isDefined) query(sqlBuilder.toPreparedStatement, sqlBuilder.parameters, dbConnection.get)
      else query(sqlBuilder.toPreparedStatement, sqlBuilder.parameters)
      val resultSet = convertResultSetToDataRows(model, rs)
      resultSet.sql = sqlBuilder.toPreparedStatement

      if (resultSet.rows.length > 0 && model.children.size > 0) {
        model.children.map {
          case (childModelName: String, childModel: Model) =>
            val parentFieldName = childModel.parentField.get
            val parentIDs: Seq[Int] = resultSet.rows.map { row =>
              row.get(parentFieldName) match {
                case Some(parentFieldValue) => parentFieldValue.asInstanceOf[TntInt].value.toInt
                case None => -1
              }
            }
            val filterForChildModel = childModel.childField.get + " In (" + parentIDs.mkString(",") + ")"
            val childRows = queryModelData(childModel, 1, Some(filterForChildModel), childModel.orderBy)
            addChildRowsToParent(resultSet, childRows)
        }
      }
      resultSet
    } catch {
      case e: Exception =>
        println("ERROR" + e.getMessage)
        val e2 = new TantalimException(e.getMessage, sqlBuilder.toPreparedStatement)
        throw e2
    }
  }

  private def parseFilterForSql(sqlBuilder: SqlBuilder, modelFields: Map[String, ModelField], filter: Option[String]): SqlBuilder = {
    if (filter.isDefined && filter.get.trim.nonEmpty) {
      val compiler = new com.tantalim.filter.compiler.CompileFilter(filter.get, modelFields)
      compiler.parse() match {
        case CompiledFilter(where: String, params: List[Any]) =>
          sqlBuilder.copy(
            where =
              if (sqlBuilder.where.isEmpty) Some(where)
              else if (where.isEmpty) sqlBuilder.where
              else Some(s"(${sqlBuilder.where.get}) AND ($where)"),
            parameters = sqlBuilder.parameters ++ params
          )
      }
    } else {
      sqlBuilder
    }
  }

  private def convertResultSetToDataRows(model: Model, rs: ResultSet) = {
    val resultBuilder = new SmartNodeSet(model)
    var done = 0
    while (rs.next() && done < 10000) {
      println("convertResultSetToDataRows")
      done = done + 1
      val newInstance = resultBuilder.insert
      model.fields.foreach {
        case (fieldName, f) =>
          newInstance.set(fieldName, {
            f.basisColumn.dataType match {
              case DataType.Integer =>
                val rsValue = rs.getInt(fieldName)
                if (rs.wasNull()) TntNull()
                else TntInt(rsValue)
              case DataType.Decimal =>
                val rsValue = rs.getBigDecimal(fieldName)
                if (rs.wasNull()) TntNull()
                else TntDecimal(rsValue)
              case DataType.String =>
                val rsValue = rs.getString(fieldName)
                if (rs.wasNull()) TntNull()
                else TntString(rsValue)
              case DataType.Boolean =>
                val rsValue = rs.getBoolean(fieldName)
                if (rs.wasNull()) TntNull()
                else TntBoolean(rsValue)
              case DataType.Date | DataType.DateTime =>
                import org.joda.time.DateTime
                val rsValue = rs.getDate(fieldName)
                if (rs.wasNull()) TntNull()
                else TntDate(new DateTime(rsValue.getTime))
              case _ => throw new MatchError(f"field.dataType of `${f.basisColumn.dataType}` is not String or Integer")
            }
          })
      }
      newInstance.id = if (model.instanceID.isDefined) newInstance.data.get(model.instanceID.get.name) else None
    }
    resultBuilder
  }

  private def addChildRowsToParent(parentRows: SmartNodeSet,
                                   childRows: SmartNodeSet): Unit = {

    var remainingChildRows = childRows.rows
    val parentField = childRows.model.parentField.get
    val childField = childRows.model.childField.get
    parentRows.rows.foreach { parentRow =>
      val parentID = parentRow.get(parentField).get
      val (matching, nonMatching) = remainingChildRows.partition { childRow =>
        parentID == childRow.get(childField).get
      }
      remainingChildRows = nonMatching
      val childSet = new SmartNodeSet(childRows.model, parentInstance = Some(parentRow))
      childSet.rows ++= matching
      parentRow.children += (childRows.model.name -> childSet)
    }
    if (remainingChildRows.length > 0) {
      throw new TantalimException(s"Failed to match ${remainingChildRows.length} ${childRows.model.name} records.",
        "Check datatypes of $parentField and $childField")
    }

  }

}
