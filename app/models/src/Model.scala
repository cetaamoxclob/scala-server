package models.src

import play.api.libs.functional.syntax._
import play.api.libs.json.Reads._
import play.api.libs.json._

case class ModelJson(basisTable: String,
                     fields: Seq[ModelFieldJson],
                     limit: Option[Int],
                     orderBy: Option[Seq[OrderBy]],
                     name: Option[String],
                     children: Option[Seq[ModelJson]])

case class ModelFieldJson(name: String,
                          basisColumn: String,
                          required: Option[Boolean],
                          updateable: Option[Boolean])

case class OrderBy(fieldName: String,
                   ascending: Option[Boolean])

object ModelJson {
  implicit def pageReads: Reads[ModelJson] = (
    (JsPath \ "basisTable").read[String] and
      (JsPath \ "fields").read[Seq[ModelFieldJson]] and
      (JsPath \ "limit").readNullable[Int] and
      (JsPath \ "orderBy").readNullable[Seq[OrderBy]] and
      (JsPath \ "name").readNullable[String] and
      (JsPath \ "children").lazyReadNullable(Reads.seq[ModelJson](pageReads))
    ).apply(ModelJson.apply _)

  implicit def pageFieldReads: Reads[ModelFieldJson] = (
    (JsPath \ "name").read[String] and
      (JsPath \ "basisColumn").read[String] and
      (JsPath \ "required").readNullable[Boolean] and
      (JsPath \ "updateable").readNullable[Boolean]
    ).apply(ModelFieldJson.apply _)

  implicit def orderByReads: Reads[OrderBy] = (
    (JsPath \ "fieldName").read[String] and
      (JsPath \ "direction").readNullable[Boolean]
    ).apply(OrderBy.apply _)
}
