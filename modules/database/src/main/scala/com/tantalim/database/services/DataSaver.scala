package com.tantalim.database.services

import java.sql.{Connection, SQLException}

import com.tantalim.models._
import com.tantalim.nodes._
import com.tantalim.util.TantalimException
import com.tantalim.database.data.SqlBuilder

import scala.collection.mutable

trait DataSaver extends DataReader {
  def saveAll(dataToSave: SmartNodeSet): Unit = {
    if (dataToSave.model.basisTable.isMock) {
      throw new TantalimException(s"Model ${dataToSave.model.name} is based on a Mock Table and cannot query the database.", "Check the calling function.")
    }
    if (dataToSave.model.instanceID.isEmpty)
      throw new TantalimException("Cannot insert/update/delete an instance without an instanceID for " + dataToSave.model.name, "")

    val connection = getConnection
    try {
      connection.setAutoCommit(false)
      saveAll(dataToSave, connection)
      connection.setAutoCommit(true)
    } catch {
      case ex: SQLException =>
        if (!connection.isClosed) {
          try {
            connection.rollback()
          } catch {
            case err: Throwable => println("failed to rollback connection: " + err)
          }
        }
        throw ex
    } finally {
      if (!connection.isClosed) {
        connection.close()
      }
    }
  }

  def saveAll(dataToSave: SmartNodeSet, dbConnection: Connection): Unit = {
    if (dataToSave.model.instanceID.isEmpty)
      throw new Exception("Cannot insert/update/delete an instance without an instanceID for " + dataToSave.model.name)

    dataToSave.foreach { dataRow =>
      dataRow.state match {
        case DataState.Inserted => insertSingleRow(dataRow, dbConnection)
        case DataState.Deleted => deleteSingleRow(dataRow, dbConnection)
        case DataState.Updated => updateSingleRow(dataRow, dbConnection)
        case DataState.ChildUpdated | DataState.Done => childUpdate(dataRow, dbConnection)
      }
    }
  }

  private def childUpdate(row: SmartNodeInstance, dbConnection: Connection): Unit = {
    row.foreachChild(childSet => saveAll(childSet, dbConnection))
    row.state = DataState.Done
  }

  def findFullUniqueIndex(indexes: Seq[TableIndex], valueMap: Map[TableColumn, TntValue]): Option[TableIndex] = {
    indexes.find { index =>
      index.columns.forall(c => valueMap.get(c).nonEmpty)
    }
  }

  private def deleteSingleRow(rowToDelete: SmartNodeInstance, dbConnection: Connection): Unit = {
    val row: SmartNodeInstance = if (rowToDelete.data.isEmpty) {
      val myFilter = Some(f"${rowToDelete.model.instanceID.get.name} = ${rowToDelete.id.get.rawString}")
      val oldDataToDelete = queryModelData(rowToDelete.nodeSet.model, filter = myFilter)
      oldDataToDelete.rows.head
    }
    else rowToDelete

    // Delete all child records first regardless of DataState
    row.foreachChild(childSet => childSet.foreach(childRow => deleteSingleRow(childRow, dbConnection)))

    val (sql, params) = createSqlForDelete(row)
    val rowCountModified = update(sql, params, dbConnection: Connection)
    if (rowCountModified != 1) {
      throw new Exception(f"Deleted $rowCountModified rows: $sql")
    }
    row.state = DataState.Done
  }

  private def updateSingleRow(row: SmartNodeInstance, dbConnection: Connection): Unit = {
    if (row.nodeSet.model.preSave.isDefined) {
      val preSaveClass = Class.forName(row.nodeSet.model.preSave.get)
      val preSaveClassObject = preSaveClass.newInstance().asInstanceOf[TantalimPreSave]
      preSaveClassObject.preSave(row)
    }

    val (sql, params) = {
      val model = row.nodeSet.model
      val primaryKey = getPrimaryKey(model)
      val primaryKeyValue = getPrimaryKeyValue(primaryKey.name, row)

      val valueMap: Map[String, TntValue] = {
        val valueMap = Map.newBuilder[String, TntValue]

        model.fields.values.foreach { field =>
          val value = getValueForInsert(row, field)
          // TODO How should we handle non-updateable fields
          if (field.step.isEmpty) {
            valueMap += field.basisColumn.dbName -> value.getOrElse(
              TntNull()
            )
          }
        }
        valueMap.result()
      }

      val fieldList = valueMap.keys.map(fieldName => fieldName).toList

      val setColumnPhrase = fieldList.map(fieldName => f"`$fieldName` = ?").mkString(", ")

      (f"UPDATE ${SqlBuilder.getTableSql(model.basisTable)} " +
        f"SET $setColumnPhrase " +
        f"WHERE `${primaryKey.basisColumn.dbName}` = ?", fieldList.map(fieldName => valueMap.get(fieldName).get).toList :+ primaryKeyValue)
    }

    val rowCountModified = update(sql, params, dbConnection)
    if (rowCountModified != 1) {
      throw new Exception(f"Update $rowCountModified rows: $sql")
    }

    childUpdate(row, dbConnection)
    row.state = DataState.Done
  }

  private def insertSingleRow(row: SmartNodeInstance, dbConnection: Connection): Unit = {
    if (row.nodeSet.model.preSave.isDefined) {
      val preSaveClass = Class.forName(row.nodeSet.model.preSave.get)
      val preSaveClassObject = preSaveClass.newInstance().asInstanceOf[TantalimPreSave]
      preSaveClassObject.preSave(row)
    }

    val (sql, params) = {
      val model = row.nodeSet.model
      val finalValueMap = {
        val rootValueMap = getValuesForTable(row, None)

        val newValues: Map[TableColumn, TntValue] = model.steps.values.flatMap { step =>
          processStepValues(row, model.fields.values, step, 0, dbConnection)
        }.toMap
        rootValueMap ++ newValues
      }

      val fieldList = finalValueMap.keys.toList
      val setColumnPhrase = fieldList.map(fieldName => f"`${fieldName.dbName}`").mkString(", ")
      val boundVars = List.fill(finalValueMap.size)("?").mkString(",")

      (f"INSERT INTO ${SqlBuilder.getTableSql(model.basisTable)} " +
        f"($setColumnPhrase) " +
        f"VALUES ($boundVars)", finalValueMap.values.toList)
    }

    val rsKeys = insert(sql, params, dbConnection)
    if (rsKeys.next()) row.setId(TntInt(rsKeys.getInt(1)))

    // Insert all child records now regardless of DataState
    row.foreachChild(childSet => childSet.foreach(childRow => insertSingleRow(childRow, dbConnection)))
    row.state = DataState.Done
  }

  /**
   *
   * This method is still a mess and is largely incomplete.
   */
  private def processStepValues(row: SmartNodeInstance,
                                modelFields: Iterable[ModelField],
                                step: ModelStep,
                                parentStep: Int,
                                dbConnection: Connection): Map[TableColumn, TntValue] = {

    if (step.parentAlias != parentStep) return Map.empty

    // TODO process this step's children(s)
    //    processStepValues(row, modelFields, st)

    val valueMap = getValuesForTable(row, Some(step))
    if (valueMap.isEmpty) return Map.empty

    println(s"Has values for step ${step.join.table.name} with $valueMap")
    step.join.table.columns.values.foreach { column =>
      val matchingField = modelFields.find { matchingField =>
        matchingField.basisColumn == column
      }
      if (matchingField.isDefined) {
        column
      }
    }

    val topIndex = findFullUniqueIndex(step.join.table.indexes, valueMap)
    if (topIndex.isEmpty) {
      println(s"Warn: Table ${step.join.table.name} is missing values to match a unique index: $valueMap")
      return Map.empty
    }

    val results = {
      val fakeModelForTable = ModelCompiler.compileModel(step.join.table.toDeep)
      val filter = valueMap.map { case (column, _) =>
        val value = column.dataType match {
          case DataType.Integer => valueMap.get(column).get match {
            case tntValue: TntInt => tntValue.value
            case tntValue: TntDecimal => tntValue.value
            case _ => Integer.parseInt(valueMap.get(column).get.rawString)
          }
          case _ => s"'${valueMap.get(column).get.rawString}'"
        }
        s"${column.name} = $value"
      }.mkString(" AND ")
      println("filter = " + filter)
      val results = queryModelData(fakeModelForTable, filter = Some(filter), dbConnection = Some(dbConnection))
      if (results.rows.length > 1) {
        throw new TantalimException("Found more than one matching row", filter)
      }
      results
    }

    val existingForeignKeyRow: Map[TableColumn, TntValue] = if (results.isEmpty) {
      if (step.allowInsert) {
        // TODO Insert new row
        Map.empty
      } else {
        //                throw new TantalimException("Foreign key doesn't exist and step doesn't allow insert", "")
        Map.empty
      }
    } else {
      val row = results.rows.head
      if (step.allowUpdate) {
        // TODO Update existing row
      }
      step.join.table.columns.values.map { column =>
        column -> row.get(column.name).get
      }.toMap
    }

    val newValues: Map[TableColumn, TntValue] = step.join.columns.filter(joinColumn => joinColumn.from.isDefined).map { joinColumn =>
      val fieldNameOnSourceTable = joinColumn.to
      val valueFromSourceTable = existingForeignKeyRow.get(fieldNameOnSourceTable).get
      val fieldNameOnDestinationTable = joinColumn.from.get

      val matchingFieldName: String = row.model.fields.values.find { field =>
        val fieldTableAlias = if (field.step.isDefined) field.step.get.tableAlias else 0
        field.basisColumn == fieldNameOnDestinationTable && parentStep == fieldTableAlias
      }.get.name
      row.set(matchingFieldName, valueFromSourceTable)

      fieldNameOnDestinationTable -> valueFromSourceTable
    }.toMap
    newValues
  }

  private def getValuesForTable(row: SmartNodeInstance, step: Option[ModelStep]): Map[TableColumn, TntValue] = {
    val model = row.nodeSet.model
    val valueMap = Map.newBuilder[TableColumn, TntValue]
    model.fields.values.foreach { field =>
      if (step == field.step) {
        val value = getValueForInsert(row, field)
        if (value.isDefined) {
          valueMap += field.basisColumn -> value.get
        }
      }
    }
    valueMap.result()
  }

  private def getValueForInsert(row: SmartNodeInstance, field: ModelField): Option[TntValue] = {

    if (row.model.childField.isDefined && row.model.parentField.isDefined) {
      if (field == row.model.getField(row.model.childField.get))
        return row.nodeSet.parentInstance.get.get(row.model.parentField.get)
    }

    if (field.alwaysDefault) {
      getDefaultValue(row, field)
    } else {
      val value = row.get(field.name)
      if (value.isDefined) {
        value.get match {
          case TntNull() => None
          case _ => Some(value.get)
        }
      } else {
        getDefaultValue(row, field)
      }
    }
  }

  private def getDefaultValue(row: SmartNodeInstance, field: ModelField): Option[TntValue] = {
    if (field.fieldDefault.isDefined) {
      val defaultValue = field.fieldDefault.get
      val valueFromDefaultField = row.get(defaultValue)
      if (valueFromDefaultField.isEmpty) {
        println("row has no value for " + defaultValue)
      }
      valueFromDefaultField
    } else if (field.valueDefault.isDefined) {
      val defaultValue = field.valueDefault.get
      field.basisColumn.dataType match {
        case DataType.Boolean => Some(TntBoolean(defaultValue == "true"))
        case _ => Some(TntString(defaultValue))
      }

    } else if (field.functionDefault.isDefined) {
      // Rule of Three - waiting for more examples of functions
      // https://code.google.com/p/scalascriptengine/
      if (field.name == "displayOrder") {
        Some(TntInt(10 + (row.index * 10)))
        None
      } else {
        None
      }
    } else None
  }

  private def createSqlForDelete(row: SmartNodeInstance): (String, List[Any]) = {
    val model = row.nodeSet.model
    val primaryKey = getPrimaryKey(model)
    val primaryKeyValue = getPrimaryKeyValue(primaryKey.name, row)

    (f"DELETE FROM ${SqlBuilder.getTableSql(model.basisTable)} " +
      f"WHERE `${primaryKey.basisColumn.dbName}` = ?", List(primaryKeyValue))
  }

  private def getPrimaryKeyValue(primaryKeyName: String, row: SmartNodeInstance) = {
    row.get(primaryKeyName).getOrElse {
      throw new Exception("Failed to find primaryKeyValue in " + row)
    }
  }

  private def getPrimaryKey(model: Model) = {
    model.fields.getOrElse(model.instanceID.get.name, {
      throw new Exception("Model does not include primary key for update")
    })
  }

}

object ModelCompiler {
  def compileModel(table: DeepTable): Model = {
    println("Creating model with table " + table.name)
    def compileField(column: TableColumn): ModelField = {
      new ModelField(
        column.name,
        column,
        updateable = column.updateable,
        required = column.required
      )
    }

    val TableOnly = "TableOnly"
    val model = new Model(
      table.name + TableOnly,
      table,
      fields = table.columns.map { case (columnName, column) =>
        columnName -> compileField(column)
      },
      instanceID = if (table.primaryKey.isDefined) Some(compileField(table.primaryKey.get)) else None,
      allowInsert = table.allowInsert,
      allowUpdate = table.allowUpdate,
      allowDelete = table.allowDelete
    )

    model
  }
}

trait TantalimPreSave {
  def preSave(row: SmartNodeInstance): Unit
}
