package compiler.src

import com.tantalim.models.ModelOrderBy
import play.api.libs.functional.syntax._
import play.api.libs.json.Reads._
import play.api.libs.json._

case class ModelJson(basisTable: Option[String],
                     fields: Option[Seq[ModelFieldJson]],
                     limit: Option[Int],
                     name: Option[String],
                     extendModel: Option[String],
                     children: Option[Seq[ModelJson]],
                     orderBy: Option[Seq[ModelOrderBy]],
                     parentLink: Option[ModelParentLink],
                     parentField: Option[String],
                     childField: Option[String],
                     steps: Option[Seq[ModelStepJson]],
                     allowInsert: Option[Boolean],
                     allowUpdate: Option[Boolean],
                     allowDelete: Option[Boolean],
                     preSave: Option[String],
                     filter: Option[String]
                      ) {
  override def toString = {
    s"Model ($name on $basisTable) Fields ($fields) "
  }
}

case class ModelStepJson(name: String,
                         join: String,
                         required: Option[Boolean],
                         parent: Option[String]
                          )

case class ModelFieldJson(name: String,
                          basisColumn: String,
                          step: Option[String],
                          required: Option[Boolean],
                          updateable: Option[Boolean],
                          fieldDefault: Option[FieldDefaultJson],
                          export: Option[Boolean]
                           ) {
  override def toString = {
    s"$basisColumn AS $name"
  }
}

case class FieldDefaultJson(value: String,
                            overwrite: Option[Boolean],
                            defaultType: Option[String],
                            watch: Option[Seq[String]])

@deprecated
case class ModelParentLink(parentField: String, childField: String)

object ModelJson {
  implicit def modelReads: Reads[ModelJson] = (
    (JsPath \ "basisTable").readNullable[String] and
      (JsPath \ "fields").readNullable[Seq[ModelFieldJson]] and
      (JsPath \ "limit").readNullable[Int] and
      (JsPath \ "name").readNullable[String] and
      (JsPath \ "extends").readNullable[String] and
      (JsPath \ "children").lazyReadNullable(Reads.seq[ModelJson](modelReads)) and
      (JsPath \ "orderBy").readNullable[Seq[ModelOrderBy]] and
      (JsPath \ "parentLink").readNullable[ModelParentLink] and
      (JsPath \ "parentField").readNullable[String] and
      (JsPath \ "childField").readNullable[String] and
      (JsPath \ "steps").readNullable[Seq[ModelStepJson]] and
      (JsPath \ "allowInsert").readNullable[Boolean] and
      (JsPath \ "allowUpdate").readNullable[Boolean] and
      (JsPath \ "allowDelete").readNullable[Boolean] and
      (JsPath \ "preSave").readNullable[String] and
      (JsPath \ "filter").readNullable[String]
    ).apply(ModelJson.apply _)

  implicit def modelFieldReads: Reads[ModelFieldJson] = (
    (JsPath \ "name").read[String] and
      (JsPath \ "basisColumn").read[String] and
      (JsPath \ "step").readNullable[String] and
      (JsPath \ "required").readNullable[Boolean] and
      (JsPath \ "updateable").readNullable[Boolean] and
      (JsPath \ "fieldDefault").readNullable[FieldDefaultJson] and
      (JsPath \ "export").readNullable[Boolean]
    ).apply(ModelFieldJson.apply _)

  implicit def fieldDefaultReads: Reads[FieldDefaultJson] = (
    (JsPath \ "value").read[String] and
      (JsPath \ "overwrite").readNullable[Boolean] and
      (JsPath \ "type").readNullable[String] and
      (JsPath \ "watch").readNullable[Seq[String]]
    ).apply(FieldDefaultJson.apply _)

  implicit def stepReads: Reads[ModelStepJson] = (
    (JsPath \ "name").read[String] and
      (JsPath \ "join").read[String] and
      (JsPath \ "required").readNullable[Boolean] and
      (JsPath \ "stepJson").readNullable[String]
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
