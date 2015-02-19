package services

import com.fasterxml.jackson.annotation.JsonValue
import data.{DataState, Database}
import models.Model
import play.api.libs.json._
import play.api.libs.functional.syntax._

case class DataRow(state: DataState,
                   data: JsObject,
                   id: Option[String],
                   tempID: Option[String],
                   children: Option[JsObject])

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
      (JsPath \ "data").read[JsObject] and
      (JsPath \ "id").readNullable[String] and
      (JsPath \ "tempID").readNullable[String] and
      (JsPath \ "children").readNullable[JsObject]
    ).apply(DataRow.apply _)
}

class DataSaver extends Database {
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
            case DataState.Inserted => insertRow(model, dataRow)
            case DataState.Deleted => deleteRow(model, dataRow)
            case DataState.Updated => updateRow(model, dataRow)
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

  private def insertRow(model: Model, row: DataRow): JsValue = {
    val sql = createSqlForInsert(model, row.data)

    val rsKeys = insert(sql)
    if (rsKeys.next()) {
      val insertedID = rsKeys.getLong(1).toString
      Json.obj(
        "id" -> insertedID,
        "data" -> (row.data + ("PersonID" -> JsString(insertedID)))
      )
    } else {
      Json.obj(
        "data" -> row.data
      )
    }
  }

  private def createSqlForInsert(model: Model, row: JsObject) = {
    import collection.mutable.ListBuffer

    val fields = ListBuffer.empty[String]
    val values = ListBuffer.empty[String]

    model.fields.values.foreach{field =>
      row \ field.name match {
        case JsString(value) => {
          println("adding field and value" + field.dbName + value)
          fields += "`" + field.dbName + "`"
          values += "'" + value.toString + "'"
        }
        case JsArray(_) | JsObject(_) => {
          throw new Exception("Found data in an incorrect format on field " + field.name)
        }
        case JsUndefined() | JsNull => {
          println("INFO: Did not find value for field = " + field.name)
        }
      }
    }

    f"INSERT INTO `${model.basisTable.dbName}` (${fields.mkString(",")}) VALUES (${values.mkString(",")})"
  }

  private def deleteRow(model: Model, row: DataRow): JsValue = {
    ???
  }

  private def updateRow(model: Model, row: DataRow): JsValue = {
    ???
  }

  private def childUpdate(model: Model, row: DataRow): JsValue = {
    ???
  }


}
