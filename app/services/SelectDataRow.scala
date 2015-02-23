package services

import play.api.libs.functional.syntax._
import play.api.libs.json.{JsPath, Writes, JsValue}

case class SelectDataRow(id: Option[String],
                         data: Map[String, JsValue],
                         var children: Option[Map[String, Seq[SelectDataRow]]])

object SelectDataRow {
  implicit def selectDataWrites: Writes[SelectDataRow] = (
    (JsPath \ "id").writeNullable[String] and
      (JsPath \ "data").write[Map[String, JsValue]] and
      (JsPath \ "children").lazyWriteNullable(Writes.map[Seq[SelectDataRow]])
    )(unlift(SelectDataRow.unapply))
}

