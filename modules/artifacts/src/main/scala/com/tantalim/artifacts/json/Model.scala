package com.tantalim.artifacts.json

import com.tantalim.models.ModelOrderBy
import play.api.libs.functional.syntax._
import play.api.libs.json.Reads._
import play.api.libs.json._

case class ModelJson(basisTable: String,
                     fields: Option[Seq[ModelFieldJson]],
                     limit: Option[Int] = None,
                     name: Option[String],
                     extendModel: Option[String] = None,
                     children: Option[Seq[ModelJson]] = None,
                     orderBy: Option[Seq[ModelOrderBy]] = None,
                     parentField: Option[String] = None,
                     childField: Option[String] = None,
                     steps: Option[Seq[ModelStepJson]] = None,
                     allowInsert: Option[Boolean] = None,
                     allowUpdate: Option[Boolean] = None,
                     allowDelete: Option[Boolean] = None,
                     preSave: Option[String] = None,
                     filter: Option[String] = None,
                     customUrlSource: Option[String] = None
                      ) {
  override def toString = {
    s"Model ($name on $basisTable) Fields ($fields) "
  }
}

case class ModelStepJson(name: String,
                         join: String,
                         required: Option[Boolean] = None,
                         steps: Option[Seq[ModelStepJson]] = None,
                         fields: Option[Seq[ModelFieldJson]] = None
                          )

case class ModelFieldJson(name: String,
                          basisColumn: Option[String],
                          dataType: Option[String] = None,
                          required: Option[Boolean] = None,
                          updateable: Option[Boolean] = None,
                          alwaysDefault: Option[Boolean] = None,
                          fieldDefault: Option[String] = None,
                          valueDefault: Option[String] = None,
                          functionDefault: Option[String] = None,
                          export: Option[Boolean] = None
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
      (JsPath \ "steps").lazyReadNullable(Reads.seq[ModelStepJson](stepReads)) and
      (JsPath \ "fields").readNullable[Seq[ModelFieldJson]]
    ).apply(ModelStepJson.apply _)

  implicit def orderByReads: Reads[ModelOrderBy] = (
    (JsPath \ "fieldName").read[String] and
      (JsPath \ "ascending").readNullable[Boolean]
    ).apply(ModelOrderBy.apply _)
}
