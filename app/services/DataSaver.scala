package services

import data.{DataState, Database}
import models.{ModelField, Model}
import play.api.libs.functional.syntax._
import play.api.libs.json._

case class DataRow(state: DataState,
                   data: Option[Map[String, JsValue]],
                   id: Option[String],
                   tempID: Option[String],
                   children: Option[Map[String, Seq[DataRow]]])

object DataSaver {

  implicit def dataStateReads: Reads[DataState] = new Reads[DataState] {
    override def reads(json: JsValue): JsResult[DataState] = {
      json match {
        case JsString(s) => {
          try {
            JsSuccess(DataState.fromString(s))
          } catch {
            case _: IllegalArgumentException => JsError(s"Failed to convert DataState value from $s")
          }
        }
        case _ => JsError("String value expected")
      }
    }
  }

  def dataRowReads: Reads[DataRow] = (
    (JsPath \ "state").read[DataState] and
      (JsPath \ "data").readNullable[Map[String, JsValue]] and
      (JsPath \ "id").readNullable[String] and
      (JsPath \ "tempID").readNullable[String] and
      (JsPath \ "children").lazyReadNullable(Reads.map(Reads.seq[DataRow](dataRowReads)))
    ).apply(DataRow.apply _)
}

class DataSaver extends DataReader with Database {
  def saveAll(model: Model, dataToSave: Option[JsValue]): JsArray = {
    dataToSave match {
      case None => return JsArray()
      case Some(jsValue) => jsValue match {
        case JsArray(jsArray) => this.savesAll(model, jsArray)
        case _ => throw new Exception("data to save should be in array format")
      }
    }
  }

  private def savesAll(model: Model, dataToSave: Seq[JsValue]): JsArray = {
    val results: Seq[DataRow] = dataToSave.map { jsValue =>
      DataSaver.dataRowReads.reads(jsValue) match {
        case JsSuccess(dataRow, _) => {
          dataRow
        }
        case JsError(err) => {
          throw new Exception("Data should be in object format " + err)
        }
      }
    }
    saveAllDataRows(model, results)
  }

  private def saveAllDataRows(model: Model, dataToSave: Seq[DataRow]): JsArray = {
    if (dataToSave.isEmpty) return JsArray()
    if (model.instanceID.isEmpty) throw new Exception("Cannot insert/update/delete an instance without an instanceID for " + model.name)

    val results: Seq[JsValue] = dataToSave.map { dataRow =>
      dataRow.state match {
        case DataState.Inserted => insertSingleRow(model, dataRow)
        case DataState.Deleted => deleteSingleRow(model, dataRow)
        case DataState.Updated => updateSingleRow(model, dataRow)
        case DataState.ChildUpdated => childUpdate(model, dataRow)
      }
    }
    JsArray(results)
  }

  private def insertSingleRow(model: Model, row: DataRow): JsValue = {
    val (sql, params) = createSqlForInsert(model, row.data.get)

    val rsKeys = insert(sql, params)
    if (rsKeys.next()) {
      val insertedID = rsKeys.getLong(1).toString
      Json.obj(
        "id" -> insertedID,
        "data" -> (row.data.get + (model.instanceID.get -> JsString(insertedID)))
      )
    } else {
      Json.obj(
        "data" -> row.data
      )
    }
  }

  private def updateSingleRow(model: Model, row: DataRow): JsValue = {
    val (sql, params) = createSqlForUpdate(model, row.data.get)
    val rowCountModified = update(sql, params)
    if (rowCountModified != 1) {
      throw new Exception(f"Update ${rowCountModified} rows: $sql")
    }
    Json.obj(
      "id" -> row.id,
      "data" -> row.data
    )
  }

  private def deleteSingleRow(model: Model, rowToDelete: DataRow): JsValue = {
    val row: DataRow = if (rowToDelete.data.isDefined) rowToDelete
    else {
      val existingRow = queryOneRow(model, rowToDelete.id.get.toInt)
      new DataRow(DataState.Deleted,
        data = Option(existingRow.data),
        id = existingRow.id,
        tempID = None,
        children = None
      )
    }
    val (sql, params) = createSqlForDelete(model, row.data.get)
    val rowCountModified = update(sql, params)
    if (rowCountModified != 1) {
      throw new Exception(f"Deleted ${rowCountModified} rows: $sql")
    }
    Json.obj(
      "id" -> row.id
    )
  }

  private def childUpdate(model: Model, row: DataRow): JsValue = {
    //    println(model)
    val allChildJson: JsObject = row.children match {
      case Some(childData) => {
        def updateChildForModel(remainingMap: Map[String, Seq[DataRow]], acc: JsObject): JsObject = {
          if (remainingMap.isEmpty) acc
          else {
            val childModelName = remainingMap.head._1
            val childData = remainingMap.head._2
            val childResults = saveAllDataRows(model.children.get(childModelName).get, childData)
            updateChildForModel(remainingMap.tail, acc + (childModelName -> childResults))
          }
        }
        updateChildForModel(childData, Json.obj())
      }
      case None => Json.obj()
    }
    Json.obj(
      "id" -> row.id,
      "data" -> row.data,
      "children" -> allChildJson
    )
  }

  private def createSqlForInsert(model: Model, row: Map[String, JsValue]): (String, List[Any]) = {
    def getColumnValues = {
      val columns = Map.newBuilder[String, Any]
      model.fields.values.foreach { field =>
        val value = convertFromJsonToScala(row, field)
        if (value.isDefined) {
          columns += ((field.dbName, value.get))
        }
      }
      columns.result
    }
    val columnValues = getColumnValues

    val setColumnPhrase = columnValues.keys.map { fieldName =>
      f"`$fieldName`"
    }.mkString(", ")
    val boundVars = columnValues.keys.map { _ => "?"}.mkString(", ")

    (f"INSERT INTO `${model.basisTable.dbName}` " +
      f"($setColumnPhrase) " +
      f"VALUES ($boundVars)", columnValues.values.toList)
  }

  private def createSqlForUpdate(model: Model, row: Map[String, JsValue]): (String, List[Any]) = {
    val primaryKey = getPrimaryKey(model)
    val primaryKeyValue = getPrimaryKeyValue(primaryKey.name, row)

    def getColumnValues = {
      val columns = Map.newBuilder[String, Any]
      model.fields.values.foreach { field =>
        if (field.updateable) {
          val value = convertFromJsonToScala(row, field)
          if (value.isDefined) {
            columns += ((field.dbName, value.get))
          }
        }
      }
      columns.result
    }
    val columnValues = getColumnValues

    val setColumnPhrase = columnValues.keys.map { fieldName =>
      f"`$fieldName` = ?"
    }.mkString(", ")

    (f"UPDATE `${model.basisTable.dbName}` " +
      f"SET $setColumnPhrase " +
      f"WHERE `${primaryKey.dbName}` = ?", columnValues.values.toList :+ primaryKeyValue)
  }

  private def convertFromJsonToScala(row: Map[String, JsValue], field: ModelField): Option[Any] = {
    row.get(field.name) match {
      case Some(jsValue) => {
        field.dataType match {
          // convert JsValue based on field.type
          case _ => Some(jsValue.as[String])
        }
      }
      case None => {
        println("Missing data for " + field.name)
        None
      }
    }

  }

  private def createSqlForDelete(model: Model, row: Map[String, JsValue]): (String, List[Any]) = {
    val primaryKey = getPrimaryKey(model)
    val primaryKeyValue = getPrimaryKeyValue(primaryKey.name, row)

    (f"DELETE FROM `${model.basisTable.dbName}` " +
      f"WHERE `${primaryKey.dbName}` = ?", List(primaryKeyValue))
  }

  private def getPrimaryKeyValue(primaryKeyName: String, row: Map[String, JsValue]) = {
    row.getOrElse(primaryKeyName, {
      throw new Exception("Data must include the table's primary key value")
    }) match {
      case JsNumber(value) => value.toLong
      case JsString(value) => value.toString
      case _ => throw new Exception("Invalid JsValue type: Must be JsNumber or JsString for " + row.get(primaryKeyName))
    }
  }

  private def getPrimaryKey(model: Model) = {
    model.fields.getOrElse(model.instanceID.get, {
      throw new Exception("Model does not include primary key for update")
    })
  }

}
