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
      steps = model.steps.values.toSeq,
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

    val sql = {
      // Create SqlBuilder without any filters
      var sqlBuilder = new SqlBuilder(
        from = model.basisTable,
        steps = model.steps.values.toSeq,
        fields = model.basisFields,
        page = page,
        orderBy = orderBy,
        limit = model.limit)
      // Add in the model-defined filter
      sqlBuilder = parseFilterForSql(sqlBuilder, model.basisFields, model.filter)
      // Add in the runtime filter
      sqlBuilder = parseFilterForSql(sqlBuilder, model.basisFields, filter)
      sqlBuilder
    }

    val resultSet = try {
      val resultSet = {
        val rs = if (dbConnection.isDefined) query(sql.toPreparedStatement, sql.parameters, dbConnection.get)
        else query(sql.toPreparedStatement, sql.parameters)
        val resultSet = convertResultSetToDataRows(model, rs)
        resultSet.sql = sql.toPreparedStatement
        resultSet
      }
      applyFieldDefaultsToResultSet(model.fields.values, resultSet)
      resultSet
    } catch {
      case e: Exception =>
        println("ERROR" + e.getMessage)
        val e2 = new TantalimException(e.getMessage, sql.toPreparedStatement)
        throw e2
    }

    if (resultSet.rows.length > 0 && model.children.size > 0) {
      model.children.map {
        case (childModelName: String, childModel: Model) =>
          val filterForChildModel = {
            val parentFieldName = childModel.parentField.get
            val parentIDs: Seq[Int] = resultSet.rows.map { row =>
              // TODO support foreignKeys of type other than Int
              row.get(parentFieldName) match {
                case Some(parentFieldValue) => parentFieldValue.asInstanceOf[TntInt].value.toInt
                case None => -1
              }
            }
            childModel.childField.get + " In (" + parentIDs.mkString(",") + ")"
          }
          val childRows = queryModelData(childModel, 1, Some(filterForChildModel), childModel.orderBy, dbConnection = dbConnection)
          addChildRowsToParent(resultSet, childRows)
      }
    }
    applyFieldDefaultsToResultSet(model.fields.values, resultSet)
    resultSet
  }

  private def parseFilterForSql(sqlBuilder: SqlBuilder, modelFields: Map[String, ModelField], filter: Option[String]): SqlBuilder = {
    if (filter.isDefined && filter.get.trim.nonEmpty) {
      try {
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
      } catch {
        case e: Exception => throw new TantalimException(s"Failed to compile filter: " + filter.get, e.getMessage)
      }
    } else {
      sqlBuilder
    }
  }

  private def convertResultSetToDataRows(model: Model, rs: ResultSet) = {
    val resultBuilder = new SmartNodeSet(model)
    while (rs.next()) {
      val newInstance = resultBuilder.insert
      model.basisFields.foreach {
        case (fieldName, f) =>
          newInstance.set(fieldName, {
            f.dataType match {
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
              case _ => throw new MatchError(f"field.dataType of `${f.dataType}` is not String or Integer")
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

  private def applyFieldDefaultsToResultSet(fields: Iterable[ModelField], set: SmartNodeSet): Unit = {
    fields.filter(_.alwaysDefault).foreach { field =>
      try {
        println(s"Defaulting ${field.name} $field")
        val valueDefault: Option[TntString] = if (field.valueDefault.isDefined) Some(TntString(field.valueDefault.get)) else None
        set.foreach { row =>
          if (valueDefault.isDefined) row.set(field.name, valueDefault.get)
          else if (field.fieldDefault.isDefined) {
            val sourceFieldName = field.fieldDefault.get
            row.set(field.name, row.get(sourceFieldName).get)
          } else if (field.functionDefault.isDefined)
            println("TODO Implement functionDefault: " + field.functionDefault.get)
        }
      } catch {
        case e: Exception => println(s"WARN: Failed to default ${field.name} due to Error: ${e.getMessage}")
        case e: TantalimException => println(s"WARN: Failed to default ${field.name} due to Error: ${e.getMessage}")
      }
    }
  }

}
