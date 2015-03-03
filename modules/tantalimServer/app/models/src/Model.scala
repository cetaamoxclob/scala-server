package models.src

import com.tantalim.models.{ModelOrderBy, ModelParentLink}
import play.api.libs.functional.syntax._
import play.api.libs.json.Reads._
import play.api.libs.json._

case class ModelJson(basisTable: String,
                     fields: Seq[ModelFieldJson],
                     limit: Option[Int],
                     name: Option[String],
                     children: Option[Seq[ModelJson]],
                     orderBy: Option[Seq[ModelOrderBy]],
                     parentLink: Option[ModelParentLink],
                     steps: Option[Seq[ModelStepJson]],
                     allowInsert: Option[Boolean],
                     allowUpdate: Option[Boolean],
                     allowDelete: Option[Boolean]
                      )

case class ModelStepJson(join: String, required: Option[Boolean])

case class ModelFieldJson(name: String,
                          basisColumn: String,
                          step: Option[String],
                          required: Option[Boolean],
                          updateable: Option[Boolean]
                           )

object ModelJson {
  implicit def modelReads: Reads[ModelJson] = (
    (JsPath \ "basisTable").read[String] and
      (JsPath \ "fields").read[Seq[ModelFieldJson]] and
      (JsPath \ "limit").readNullable[Int] and
      (JsPath \ "name").readNullable[String] and
      (JsPath \ "children").lazyReadNullable(Reads.seq[ModelJson](modelReads)) and
      (JsPath \ "orderBy").readNullable[Seq[ModelOrderBy]] and
      (JsPath \ "parentLink").readNullable[ModelParentLink] and
      (JsPath \ "steps").readNullable[Seq[ModelStepJson]] and
      (JsPath \ "allowInsert").readNullable[Boolean] and
      (JsPath \ "allowUpdate").readNullable[Boolean] and
      (JsPath \ "allowDelete").readNullable[Boolean]
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
      (JsPath \ "required").readNullable[Boolean]
    ).apply(ModelStepJson.apply _)

  implicit def parentLinkReads: Reads[ModelParentLink] = (
    (JsPath \ "parentField").read[String] and
      (JsPath \ "childField").read[String]
    ).apply(ModelParentLink.apply _)

  implicit def orderByReads: Reads[ModelOrderBy] = (
    (JsPath \ "fieldName").read[String] and
      (JsPath \ "direction").readNullable[Boolean]
    ).apply(ModelOrderBy.apply _)
}
