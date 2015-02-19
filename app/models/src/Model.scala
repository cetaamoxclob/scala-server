package models.src

import play.api.libs.functional.syntax._
import play.api.libs.json.Reads._
import play.api.libs.json._

case class ModelJson(basisTable: String,
                     fields: Seq[ModelFieldJson],
                     limit: Option[Int],
                     name: Option[String],
                     children: Option[Seq[ModelJson]],
                     orderBy: Option[Seq[OrderByJson]],
                     steps: Option[Seq[ModelStepJson]]
                      )

// TODO Support steps that are more than 1 join away from basis table
case class ModelStepJson(join: String,
                         fields: Seq[ModelFieldJson],
                         required: Option[Boolean]
                          )

case class ModelFieldJson(name: String,
                          basisColumn: String,
                          step: Option[String],
                          required: Option[Boolean],
                          updateable: Option[Boolean]
                           )

case class OrderByJson(fieldName: String,
                       ascending: Option[Boolean]
                        )

object ModelJson {
  implicit def modelReads: Reads[ModelJson] = (
    (JsPath \ "basisTable").read[String] and
      (JsPath \ "fields").read[Seq[ModelFieldJson]] and
      (JsPath \ "limit").readNullable[Int] and
      (JsPath \ "name").readNullable[String] and
      (JsPath \ "children").lazyReadNullable(Reads.seq[ModelJson](modelReads)) and
      (JsPath \ "orderBy").readNullable[Seq[OrderByJson]] and
      (JsPath \ "steps").readNullable[Seq[ModelStepJson]]
    ).apply(ModelJson.apply _)

  implicit def modelFieldReads: Reads[ModelFieldJson] = (
    (JsPath \ "name").read[String] and
      (JsPath \ "basisColumn").read[String] and
      (JsPath \ "step").readNullable[String] and
      (JsPath \ "required").readNullable[Boolean] and
      (JsPath \ "updateable").readNullable[Boolean]
    ).apply(ModelFieldJson.apply _)

  implicit def stepReads: Reads[ModelStepJson] = (
    (JsPath \ "join").read[String] and
      (JsPath \ "fields").read[Seq[ModelFieldJson]] and
      (JsPath \ "required").readNullable[Boolean]
    ).apply(ModelStepJson.apply _)

  implicit def orderByReads: Reads[OrderByJson] = (
    (JsPath \ "fieldName").read[String] and
      (JsPath \ "direction").readNullable[Boolean]
    ).apply(OrderByJson.apply _)
}
