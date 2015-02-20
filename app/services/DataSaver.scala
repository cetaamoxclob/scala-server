package services

import data.{DataState, Database}
import models.Model
import play.api.libs.functional.syntax._
import play.api.libs.json._

case class DataRow(state: DataState,
                   data: Option[Map[String, JsValue]],
                   id: Option[String],
                   tempID: Option[String],
                   children: Option[Map[String, DataRow]])

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
      (JsPath \ "children").lazyReadNullable(Reads.map[DataRow](dataRowReads))
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
    if (dataToSave.isEmpty) return JsArray()
    if (model.instanceID.isEmpty) throw new Exception("Cannot insert/update/delete an instance without an instanceID for " + model.name)

    val results: Seq[JsValue] = dataToSave.map { jsValue =>
      DataSaver.dataRowReads.reads(jsValue) match {
        case JsSuccess(dataRow, _) => {
          dataRow.state match {
            case DataState.Inserted => insertSingleRow(model, dataRow)
            case DataState.Deleted => deleteSingleRow(model, dataRow)
            case DataState.Updated => updateSingleRow(model, dataRow)
            case DataState.ChildUpdated => ???
          }
        }
        case JsError(err) => {
          throw new Exception("Data should be in object format " + err)
        }
      }
    }
    JsArray(results)
  }

  private def insertSingleRow(model: Model, row: DataRow): JsValue = {
    val sql = createSqlForInsert(model, row.data.get)

    val rsKeys = insert(sql)
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
    val sql = createSqlForUpdate(model, row.data.get)
    val rowCountModified = update(sql)
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
    val sql = createSqlForDelete(model, row.data.get)
    val rowCountModified = update(sql)
    if (rowCountModified != 1) {
      throw new Exception(f"Deleted ${rowCountModified} rows: $sql")
    }
    Json.obj(
      "id" -> row.id
    )
  }

  private def childUpdate(model: Model, row: DataRow): JsValue = {
    ???
  }

  private def createSqlForInsert(model: Model, row: Map[String, JsValue]) = {
    import scala.collection.mutable.ListBuffer

    val fields = ListBuffer.empty[String]
    val values = ListBuffer.empty[String]

    model.fields.values.foreach { field =>
      row.get(field.name) match {
        case Some(value) => {
          println("adding field and value" + field.dbName + value)
          fields += "`" + field.dbName + "`"
          values += "'" + value.as[String] + "'"
        }
        case None => {
          println("Missing data for " + field.name)
        }
      }
    }

    f"INSERT INTO `${model.basisTable.dbName}` (${fields.mkString(",")}) VALUES (${values.mkString(",")})"
  }

  private def createSqlForUpdate(model: Model, row: Map[String, JsValue]) = {

    val primaryKey = model.fields.getOrElse(model.instanceID.get, {
      throw new Exception("Model does not include primary key for update")
    })
    val primaryKeyValue = row.getOrElse(primaryKey.name, {
      throw new Exception("Data must include the table's primary key value")
    }).as[String]

    val columns = Map.newBuilder[String, String]
    model.fields.values.foreach { field =>
      val value = row.get(field.name) match {
        case Some(value) => {
          Some("'" + value.as[String] + "'")
        }
        case None => {
          println("Missing data for " + field.name)
          None
        }
      }
      if (value.isDefined && field.updateable) {
        columns += ((field.dbName, value.get))
      }
    }

    val setColumnPhrase = columns.result.map { value =>
      f"`${value._1}` = ${value._2}"
    }.mkString(", ")

    f"UPDATE `${model.basisTable.dbName}` SET ${setColumnPhrase} WHERE `${primaryKey.dbName}` = '${primaryKeyValue}'"
  }

  private def createSqlForDelete(model: Model, row: Map[String, JsValue]) = {

    val primaryKey = model.fields.getOrElse(model.instanceID.get, {
      throw new Exception("Model does not include primary key for update")
    })
    val primaryKeyValue = row.getOrElse(primaryKey.name, {
      throw new Exception("Data must include the table's primary key value")
    }) match {
      case JsNumber(value) => value.toLong
      case JsString(value) => value.toString
      case _ => throw new Exception("Invalid JsValue type: Must be JsNumber of JsString for " + row.get(primaryKey.name))
    }

    f"DELETE FROM `${model.basisTable.dbName}` WHERE `${primaryKey.dbName}` = ${primaryKeyValue}"
  }

}
