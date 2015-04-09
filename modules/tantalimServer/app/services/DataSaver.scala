package services

import java.sql.{SQLException, Connection}

import com.tantalim.nodes._
import com.tantalim.util.TantalimException
import compiler.ModelCompiler
import data._
import com.tantalim.models._

import scala.collection.mutable

trait DataSaver extends DataReader with ModelCompiler with DatabaseConnection {
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

  private def updateSingleRow(row: SmartNodeInstance, dbConnection: Connection): Unit = {
    val (sql, params) = createSqlForUpdate(row)

    val rowCountModified = update(sql, params, dbConnection)
    if (rowCountModified != 1) {
      throw new Exception(f"Update $rowCountModified rows: $sql")
    }

    childUpdate(row, dbConnection)
    row.state = DataState.Done
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

  private def childUpdate(row: SmartNodeInstance, dbConnection: Connection): Unit = {
    row.foreachChild(childSet => saveAll(childSet, dbConnection))
    row.state = DataState.Done
  }

  def findFullUniqueIndex(indexes: Seq[TableIndex], valueMap: mutable.Map[TableColumn, TntValue]): Option[TableIndex] = {
    indexes.find { index =>
      index.columns.forall(c => valueMap.get(c).nonEmpty)
    }
  }

  private def insertSingleRow(row: SmartNodeInstance, dbConnection: Connection): Unit = {
    if (row.nodeSet.model.preSave.isDefined) {
      val preSaveClass = Class.forName(row.nodeSet.model.preSave.get)
      val preSaveClassObject = preSaveClass.newInstance().asInstanceOf[TantalimPreSave]
      preSaveClassObject.preSave(row)
    }

    val (sql, params) = {
      val model = row.nodeSet.model
      val rootValueMap = getValuesForTable(row, None)

      model.steps.values.foreach { step =>
        val valueMap = getValuesForTable(row, Some(step))

        if (valueMap.nonEmpty) {
          println(s"Has values for step ${step.join.table.name} with $valueMap")
          step.join.table.columns.values.foreach { column =>
            val matchingField = model.fields.values.find { matchingField =>
              matchingField.basisColumn == column
            }
            if (matchingField.isDefined) {
              column
            }
          }

          val topIndex = findFullUniqueIndex(step.join.table.indexes, valueMap)
          if (topIndex.isEmpty) {
            println(s"Warn: Table ${step.join.table.name} is missing unique index")
//            throw new TantalimException(s"Table ${step.join.table.name} is missing unique index", "")
          } else {
            val fakeModelForTable = compileModel(step.join.table.toDeep)
            val filter = valueMap.map { case (column, _) =>
              val value = column.dataType match {
                case DataType.Integer => valueMap.get(column).get.asInstanceOf[TntInt].value
                case _ => s"'${valueMap.get(column).get.rawString}'"
              }
              s"${column.name} = $value"
            }.mkString(" AND ")
            println("filter = " + filter)
            val results = queryModelData(fakeModelForTable, filter = Some(filter), dbConnection = Some(dbConnection))

            val existingForeignKeyRow: Map[TableColumn, TntValue] = if (results.isEmpty) {
              if (step.allowInsert) {
                // TODO Insert new row
                Map.empty
              } else {
//                throw new TantalimException("Foreign key doesn't exist and step doesn't allow insert", "")
                Map.empty
              }
            } else {
              if (results.rows.length > 1) {
                throw new TantalimException("Found more than one matching row", filter)
              }
              val row = results.rows.head
              if (step.allowUpdate) {
                // TODO Update existing row
              }
              step.join.table.columns.values.map { column =>
                column -> row.get(column.name).get
              }.toMap
            }

            step.join.columns.foreach { joinColumn =>
              if (joinColumn.from.isDefined) {
                val fieldNameOnSourceTable = joinColumn.to
                val valueFromSourceTable = existingForeignKeyRow.get(fieldNameOnSourceTable).get
                val fieldNameOnDestinationTable = joinColumn.from.get
                rootValueMap += fieldNameOnDestinationTable -> valueFromSourceTable
              }
            }
          }
        }
      }

      val fieldList = rootValueMap.keys.toList
      val setColumnPhrase = fieldList.map(fieldName => f"`${fieldName.dbName}`").mkString(", ")
      val boundVars = List.fill(rootValueMap.size)("?").mkString(",")

      (f"INSERT INTO ${SqlBuilder.getTableSql(model.basisTable)} " +
        f"($setColumnPhrase) " +
        f"VALUES ($boundVars)", rootValueMap.values.toList)
    }

    val rsKeys = insert(sql, params, dbConnection)
    if (rsKeys.next()) row.setId(TntInt(rsKeys.getInt(1)))

    // Insert all child records now regardless of DataState
    row.foreachChild(childSet => childSet.foreach(childRow => insertSingleRow(childRow, dbConnection)))
    row.state = DataState.Done
  }

  private def getValuesForTable(row: SmartNodeInstance, step: Option[ModelStep]): collection.mutable.Map[TableColumn, TntValue] = {
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
    collection.mutable.Map.empty ++ valueMap.result()
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

  private def createSqlForUpdate(row: SmartNodeInstance): (String, List[Any]) = {
    val model = row.nodeSet.model
    val primaryKey = getPrimaryKey(model)
    val primaryKeyValue = getPrimaryKeyValue(primaryKey.name, row)

    val valueMap: Map[String, TntValue] = {
      val valueMap = Map.newBuilder[String, TntValue]

      model.fields.values.foreach { field =>
        if (field.updateable && field.step.isEmpty) {
          val value = row.get(field.name)
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

trait TantalimPreSave {
  def preSave(row: SmartNodeInstance): Unit
}

class DataSaverService extends DataSaver
