package data

import play.api.libs.json._
import play.api.libs.functional.syntax._

case class DataInstance(var state: DataState = DataState.Done,
                        data: Option[Map[String, JsValue]],
                        id: Option[String] = None,
                        tempID: Option[String] = None,
                        var children: Option[Map[String, Seq[DataInstance]]] = None)

object DataInstance {
  implicit def selectDataWrites: Writes[DataInstance] = (
    (JsPath \ "state").write[DataState] and
      (JsPath \ "data").writeNullable[Map[String, JsValue]] and
      (JsPath \ "id").writeNullable[String] and
      (JsPath \ "tempID").writeNullable[String] and
      (JsPath \ "children").lazyWriteNullable(Writes.map[Seq[DataInstance]])
    )(unlift(DataInstance.unapply))

  implicit def dataRowReads: Reads[DataInstance] = (
    (JsPath \ "state").read[DataState] and
      (JsPath \ "data").readNullable[Map[String, JsValue]] and
      (JsPath \ "id").readNullable[String] and
      (JsPath \ "tempID").readNullable[String] and
      (JsPath \ "children").lazyReadNullable(Reads.map(Reads.seq[DataInstance](dataRowReads)))
    ).apply(DataInstance.apply _)

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

  implicit def dataStateWrites: Writes[DataState] = new Writes[DataState] {
    override def writes(o: DataState): JsValue = JsString(o.toString)
  }

}
