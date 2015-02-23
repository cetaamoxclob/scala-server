package services

import data.DataState
import play.api.libs.json._
import play.api.libs.functional.syntax._

case class DataRow(state: DataState,
                   data: Option[Map[String, JsValue]],
                   id: Option[String] = None,
                   tempID: Option[String] = None,
                   children: Option[Map[String, Seq[DataRow]]] = None)

object DataRow {
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

