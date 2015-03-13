package services

import java.sql.{SQLException, Connection}

import com.tantalim.nodes._
import data._
import com.tantalim.models.{DataType, FieldDefaultType, ModelField, Model}

trait DataSaver extends DataReader with Database {
  def saveAll(dataToSave: SmartNodeSet): Unit = {
    if (dataToSave.model.instanceID.isEmpty)
      throw new Exception("Cannot insert/update/delete an instance without an instanceID for " + dataToSave.model.name)

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

  private def insertSingleRow(row: SmartNodeInstance, dbConnection: Connection): Unit = {
    if (row.nodeSet.model.preSave.isDefined) {
      val preSaveClass = Class.forName(row.nodeSet.model.preSave.get)
      val preSaveClassObject = preSaveClass.newInstance().asInstanceOf[TantalimPreSave]
      preSaveClassObject.preSave(row)
    }

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
      val myFilter = Some(f"${rowToDelete.model.instanceID.get} = ${rowToDelete.id.get.rawString}")
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
        val value = getValueForInsert(row, field)
        if (value.isDefined) {
          columnNames += field.basisColumn.dbName
          columnValues += value.get
        }
      }

      if (row.model.parentLink.isDefined) {
        columnNames += row.model.fields.get(row.model.parentLink.get.childField).get.basisColumn.dbName
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

  private def getValueForInsert(row: SmartNodeInstance, field: ModelField): Option[TntValue] = {

    if (field.fieldDefault.isDefined && field.fieldDefault.get.overwrite) {
      getDefaultValue(row, field)
    } else {
      val value = row.get(field.name)
      if (value.isDefined) {
        value.get match {
          case TntNull() => None
          case _ => Some(value.get)
        }
      } else {
        if (field.fieldDefault.isDefined) {
          getDefaultValue(row, field)
        } else None
      }
    }
  }

  private def getDefaultValue(row: SmartNodeInstance, field: ModelField): Option[TntValue] = {
    val default = field.fieldDefault.get
    default.defaultType match {
      case FieldDefaultType.Constant =>
        field.basisColumn.dataType match {
          case DataType.Boolean => Some(TntBoolean(default.value == "true"))
          case _ => Some(TntString(default.value))
        }
      case FieldDefaultType.Field => row.get(default.value)
      case FieldDefaultType.Fxn =>
        // Rule of Three - waiting for more examples of functions
        // https://code.google.com/p/scalascriptengine/
        if (field.name == "displayOrder") {
          Some(TntInt(10 + (row.index * 10)))
          None
        } else {
          None
        }
    }
  }

  private def createSqlForUpdate(row: SmartNodeInstance): (String, List[Any]) = {
    val model = row.nodeSet.model
    val primaryKey = getPrimaryKey(model)
    val primaryKeyValue = getPrimaryKeyValue(primaryKey.name, row)

    val (columnNames, columnValues) = {
      val columnNames = List.newBuilder[String]
      val columnValues = List.newBuilder[TntValue]

      model.fields.values.foreach { field =>
        if (field.updateable && field.step.isEmpty) {
          val value = row.get(field.name)
          columnNames += field.basisColumn.dbName
          columnValues += value.getOrElse(
            TntNull()
          )
        }
      }

      (columnNames.result(), columnValues.result())
    }

    val setColumnPhrase = columnNames.map(fieldName => f"`$fieldName` = ?").mkString(", ")

    (f"UPDATE `${model.basisTable.dbName}` " +
      f"SET $setColumnPhrase " +
      f"WHERE `${primaryKey.basisColumn.dbName}` = ?", columnValues.toList :+ primaryKeyValue)
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

trait TantalimPreSave {
  def preSave(row: SmartNodeInstance): Unit
}

class DataSaverService extends DataSaver
