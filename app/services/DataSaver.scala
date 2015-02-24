package services

import data._
import models.Model

trait DataSaver extends DataReader with Database {
  def saveAll(dataToSave: SmartNodeSet): Unit = {
    if (dataToSave.model.instanceID.isEmpty)
      throw new Exception("Cannot insert/update/delete an instance without an instanceID for " + dataToSave.model.name)

    dataToSave.rows.foreach { dataRow =>
      dataRow.state match {
        case DataState.Inserted => insertSingleRow(dataRow)
        case DataState.Deleted => deleteSingleRow(dataRow)
        case DataState.Updated => updateSingleRow(dataRow)
        case DataState.ChildUpdated | DataState.Done => childUpdate(dataRow)
      }
    }
  }

  def insertSingleRow(row: SmartNodeInstance): Unit = {
    val (sql, params) = createSqlForInsert(row)

    val rsKeys = insert(sql, params)
    if (rsKeys.next()) row.setId(TntInt(rsKeys.getInt(1)))

    // Insert all child records now regardless of DataState
    row.foreachChild(childSet => childSet.foreach(childRow => insertSingleRow(childRow)))
  }

  def updateSingleRow(row: SmartNodeInstance): Unit = {
    val (sql, params) = createSqlForUpdate(row)

    val rowCountModified = update(sql, params)
    if (rowCountModified != 1) {
      throw new Exception(f"Update $rowCountModified rows: $sql")
    }

    childUpdate(row)
  }

  def deleteSingleRow(rowToDelete: SmartNodeInstance): Unit = {
    val row: SmartNodeInstance = if (rowToDelete.data.isEmpty) {
      queryOneRow(rowToDelete.nodeSet.model, rowToDelete.id.get).getOrElse(
        // Maybe we should just exit and not worry about this
        throw new Exception("Failed to select data to delete. Maybe the row was already deleted")
      )
    }
    else rowToDelete

    // Delete all child records first regardless of DataState
    row.foreachChild(childSet => childSet.foreach(childRow => deleteSingleRow(childRow)))

    val (sql, params) = createSqlForDelete(row)
    val rowCountModified = update(sql, params)
    if (rowCountModified != 1) {
      throw new Exception(f"Deleted $rowCountModified rows: $sql")
    }
  }

  private def childUpdate(row: SmartNodeInstance): Unit = {
    row.foreachChild(childSet => saveAll(childSet))
  }

  private def createSqlForInsert(row: SmartNodeInstance): (String, List[TntValue]) = {
    val model = row.nodeSet.model
    def getColumnValues = {
      val columns = Map.newBuilder[String, TntValue]
      model.fields.values.foreach { field =>
        val value = row.get(field.name)
        if (value.isDefined) {
          columns += ((field.basisColumn.dbName, value.get))
        }
      }
      columns.result()
    }
    val columnValues = getColumnValues
    val setColumnPhrase = columnValues.keys.map { fieldName =>
      f"`$fieldName`"
    }.mkString(", ")
    val boundVars = List.fill(columnValues.size)("?").mkString(",")

    (f"INSERT INTO `${model.basisTable.dbName}` " +
      f"($setColumnPhrase) " +
      f"VALUES ($boundVars)", columnValues.values.toList)
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
