package services

import java.sql.{SQLException, Connection}

import data._
import models.Model

trait DataSaver extends DataReader with Database {
  def saveAll(dataToSave: SmartNodeSet): Unit = {
    if (dataToSave.model.instanceID.isEmpty)
      throw new Exception("Cannot insert/update/delete an instance without an instanceID for " + dataToSave.model.name)

    val connection = getConnection()
    // See http://docs.oracle.com/javase/tutorial/jdbc/basics/transactions.html
    try {
      connection.setAutoCommit(false)
      saveAll(dataToSave, connection)
    } catch {
      case ex: SQLException => {
        connection.rollback()
        throw ex
      }
    } finally {
      connection.setAutoCommit(true)
      connection.close()
    }
  }

  def saveAll(dataToSave: SmartNodeSet, dbConnection: Connection): Unit = {
    if (dataToSave.model.instanceID.isEmpty)
      throw new Exception("Cannot insert/update/delete an instance without an instanceID for " + dataToSave.model.name)


    dataToSave.rows.foreach { dataRow =>
      dataRow.state match {
        case DataState.Inserted => insertSingleRow(dataRow, dbConnection)
        case DataState.Deleted => deleteSingleRow(dataRow, dbConnection)
        case DataState.Updated => updateSingleRow(dataRow, dbConnection)
        case DataState.ChildUpdated | DataState.Done => childUpdate(dataRow, dbConnection)
      }
    }
  }


  private def insertSingleRow(row: SmartNodeInstance, dbConnection: Connection): Unit = {
    val (sql, params) = createSqlForInsert(row)

    val rsKeys = insert(sql, params, dbConnection)
    if (rsKeys.next()) row.setId(TntInt(rsKeys.getInt(1)))

    // Insert all child records now regardless of DataState
    row.foreachChild(childSet => childSet.foreach(childRow => insertSingleRow(childRow, dbConnection)))
    row.state = DataState.Done
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
      val myFilter = Some(f"${rowToDelete.model.instanceID.get} = ${rowToDelete.id.get}")
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

  private def createSqlForInsert(row: SmartNodeInstance): (String, List[TntValue]) = {
    val model = row.nodeSet.model
    val (columnNames, columnValues) = {
      val columnNames = List.newBuilder[String]
      val columnValues = List.newBuilder[TntValue]

      model.fields.values.foreach { field =>
        val value = row.get(field.name)
        if (value.isDefined) {
          value.get match {
            case TntNull() => // Don't bother inserting nulls
            case _ =>
              columnNames += field.basisColumn.dbName
              columnValues += value.get
          }
        }
      }

      if (row.model.parentLink.isDefined) {
        columnNames += row.model.parentLink.get.childField
        columnValues += row.nodeSet.parentInstance.get.get(row.model.parentLink.get.parentField).get
      }
      (columnNames.result(), columnValues.result())
    }

    val setColumnPhrase = columnNames.map(fieldName => f"`$fieldName`").mkString(", ")
    val boundVars = List.fill(columnNames.size)("?").mkString(",")

    (f"INSERT INTO `${model.basisTable.dbName}` " +
      f"($setColumnPhrase) " +
      f"VALUES ($boundVars)", columnValues.toList)
  }

  private def createSqlForUpdate(row: SmartNodeInstance): (String, List[Any]) = {
    val model = row.nodeSet.model
    val primaryKey = getPrimaryKey(model)
    val primaryKeyValue = getPrimaryKeyValue(primaryKey.name, row)

    def getColumnValues = {
      val columns = Map.newBuilder[String, Any]
      model.fields.values.foreach { field =>
        if (field.updateable) {
          val value = row.get(field.name)
          if (value.isDefined) {
            columns += ((field.basisColumn.dbName, value.get))
          }
        }
      }
      columns.result()
    }
    val columnValues = getColumnValues

    val setColumnPhrase = columnValues.keys.map { fieldName =>
      f"`$fieldName` = ?"
    }.mkString(", ")

    (f"UPDATE `${model.basisTable.dbName}` " +
      f"SET $setColumnPhrase " +
      f"WHERE `${primaryKey.basisColumn.dbName}` = ?", columnValues.values.toList :+ primaryKeyValue)
  }

  private def createSqlForDelete(row: SmartNodeInstance): (String, List[Any]) = {
    val model = row.nodeSet.model
    val primaryKey = getPrimaryKey(model)
    val primaryKeyValue = getPrimaryKeyValue(primaryKey.name, row)

    (f"DELETE FROM `${model.basisTable.dbName}` " +
      f"WHERE `${primaryKey.basisColumn.dbName}` = ?", List(primaryKeyValue))
  }

  private def getPrimaryKeyValue(primaryKeyName: String, row: SmartNodeInstance) = {
    row.get(primaryKeyName).getOrElse {
      throw new Exception("Failed to find primaryKeyValue in " + row)
    }
  }

  private def getPrimaryKey(model: Model) = {
    model.fields.getOrElse(model.instanceID.get, {
      throw new Exception("Model does not include primary key for update")
    })
  }

}

class DataSaverService extends DataSaver
