package com.tantalim.artifacts.json

import com.tantalim.models.ModelOrderBy
import play.api.libs.functional.syntax._
import play.api.libs.json.Reads._
import play.api.libs.json._

case class ModelJson(basisTable: String,
                     fields: Option[Seq[ModelFieldJson]],
                     limit: Option[Int],
                     name: Option[String],
                     extendModel: Option[String],
                     children: Option[Seq[ModelJson]],
                     orderBy: Option[Seq[ModelOrderBy]],
                     parentField: Option[String],
                     childField: Option[String],
                     steps: Option[Seq[ModelStepJson]],
                     allowInsert: Option[Boolean],
                     allowUpdate: Option[Boolean],
                     allowDelete: Option[Boolean],
                     preSave: Option[String],
                     filter: Option[String],
                     customUrlSource: Option[String]
                      ) {
  override def toString = {
    s"Model ($name on $basisTable) Fields ($fields) "
  }
}

case class ModelStepJson(name: String,
                         join: String,
                         required: Option[Boolean],
                         parent: Option[String],
                         fields: Option[Seq[ModelFieldJson]]
                          )

case class ModelFieldJson(name: String,
                          basisColumn: Option[String],
                          dataType: Option[String],
                          @deprecated
                          step: Option[String],
                          required: Option[Boolean],
                          updateable: Option[Boolean],
                          alwaysDefault: Option[Boolean],
                          fieldDefault: Option[String],
                          valueDefault: Option[String],
                          functionDefault: Option[String],
                          export: Option[Boolean]
                           ) {
  override def toString = {
    s"$basisColumn AS $name"
  }
}

object ModelJson {
  implicit def modelReads: Reads[ModelJson] = (
    (JsPath \ "basisTable").read[String] and
      (JsPath \ "fields").readNullable[Seq[ModelFieldJson]] and
      (JsPath \ "limit").readNullable[Int] and
      (JsPath \ "name").readNullable[String] and
      (JsPath \ "extends").readNullable[String] and
      (JsPath \ "children").lazyReadNullable(Reads.seq[ModelJson](modelReads)) and
      (JsPath \ "orderBy").readNullable[Seq[ModelOrderBy]] and
      (JsPath \ "parentField").readNullable[String] and
      (JsPath \ "childField").readNullable[String] and
      (JsPath \ "steps").readNullable[Seq[ModelStepJson]] and
      (JsPath \ "allowInsert").readNullable[Boolean] and
      (JsPath \ "allowUpdate").readNullable[Boolean] and
      (JsPath \ "allowDelete").readNullable[Boolean] and
      (JsPath \ "preSave").readNullable[String] and
      (JsPath \ "filter").readNullable[String] and
      (JsPath \ "customUrlSource").readNullable[String]
    ).apply(ModelJson.apply _)

  implicit def modelFieldReads: Reads[ModelFieldJson] = (
    (JsPath \ "name").read[String] and
      (JsPath \ "basisColumn").readNullable[String] and
      (JsPath \ "dataType").readNullable[String] and
      (JsPath \ "step").readNullable[String] and
      (JsPath \ "required").readNullable[Boolean] and
      (JsPath \ "updateable").readNullable[Boolean] and
      (JsPath \ "alwaysDefault").readNullable[Boolean] and
      (JsPath \ "fieldDefault").readNullable[String] and
      (JsPath \ "valueDefault").readNullable[String] and
      (JsPath \ "functionDefault").readNullable[String] and
      (JsPath \ "export").readNullable[Boolean]
    ).apply(ModelFieldJson.apply _)

  implicit def stepReads: Reads[ModelStepJson] = (
    (JsPath \ "name").read[String] and
      (JsPath \ "join").read[String] and
      (JsPath \ "required").readNullable[Boolean] and
      (JsPath \ "parent").readNullable[String] and
      (JsPath \ "fields").readNullable[Seq[ModelFieldJson]]
    ).apply(ModelStepJson.apply _)

  implicit def orderByReads: Reads[ModelOrderBy] = (
    (JsPath \ "fieldName").read[String] and
      (JsPath \ "ascending").readNullable[Boolean]
    ).apply(ModelOrderBy.apply _)
}
