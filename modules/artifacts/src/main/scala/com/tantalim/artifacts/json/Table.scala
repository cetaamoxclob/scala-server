package com.tantalim.artifacts.json

import play.api.libs.functional.syntax._
import play.api.libs.json.Reads._
import play.api.libs.json._

case class TableJson(dbName: Option[String],
                     module: Option[String],
                     primaryKey: Option[String],
                     columns: Seq[TableColumnJson],
                     joins: Option[Seq[TableJoinJson]],
                     indexes: Option[Seq[TableIndexJson]],
                     allowInsert: Option[Boolean],
                     allowUpdate: Option[Boolean],
                     allowDelete: Option[Boolean]
                      )

case class TableColumnJson(name: String,
                           dbName: Option[String],
                           dataType: Option[String],
                           updateable: Option[Boolean],
                           required: Option[Boolean],
                           label: Option[String],
                           help: Option[String],
                           placeholder: Option[String],
                           fieldType: Option[String],
                           length: Option[Int],
                           columnDefault: Option[String]
                            )

case class TableJoinJson(name: String,
                         table: String,
                         required: Option[Boolean],
                         columns: Seq[TableJoinColumnJson])

case class TableJoinColumnJson(to: String,
                               from: Option[String],
                               fromText: Option[String])

case class TableIndexJson(unique: Option[Boolean],
                          columns: Seq[TableIndexColumnJson])

case class TableIndexColumnJson(name: String,
                                ignoreMe: Option[String])

object TableJson {
  implicit def tableReads: Reads[TableJson] = (
    (JsPath \ "dbName").readNullable[String] and
      (JsPath \ "module").readNullable[String] and
      (JsPath \ "primaryKey").readNullable[String] and
      (JsPath \ "columns").read[Seq[TableColumnJson]] and
      (JsPath \ "joins").readNullable[Seq[TableJoinJson]] and
      (JsPath \ "indexes").readNullable[Seq[TableIndexJson]] and
      (JsPath \ "allowInsert").readNullable[Boolean] and
      (JsPath \ "allowUpdate").readNullable[Boolean] and
      (JsPath \ "allowDelete").readNullable[Boolean]
    ).apply(TableJson.apply _)

  implicit def tableColumnReads: Reads[TableColumnJson] = (
    (JsPath \ "name").read[String] and
      (JsPath \ "dbName").readNullable[String] and
      (JsPath \ "dataType").readNullable[String] and
      (JsPath \ "updateable").readNullable[Boolean] and
      (JsPath \ "required").readNullable[Boolean] and
      (JsPath \ "label").readNullable[String] and
      (JsPath \ "help").readNullable[String] and
      (JsPath \ "placeholder").readNullable[String] and
      (JsPath \ "fieldType").readNullable[String] and
      (JsPath \ "length").readNullable[Int] and
      (JsPath \ "columnDefault").readNullable[String]
    ).apply(TableColumnJson.apply _)

  implicit def tableJoinReads: Reads[TableJoinJson] = (
    (JsPath \ "name").read[String] and
      (JsPath \ "table").read[String] and
      (JsPath \ "required").readNullable[Boolean] and
      (JsPath \ "columns").read[Seq[TableJoinColumnJson]]
    ).apply(TableJoinJson.apply _)

  implicit def tableJoinColumnReads: Reads[TableJoinColumnJson] = (
    (JsPath \ "to").read[String] and
      (JsPath \ "from").readNullable[String] and
      (JsPath \ "fromText").readNullable[String]
    ).apply(TableJoinColumnJson.apply _)

  implicit def tableIndexReads: Reads[TableIndexJson] = (
      (JsPath \ "unique").readNullable[Boolean] and
      (JsPath \ "columns").read[Seq[TableIndexColumnJson]]
    ).apply(TableIndexJson.apply _)

  implicit def tableIndexColumnReads: Reads[TableIndexColumnJson] = (
    (JsPath \ "name").read[String] and
      (JsPath \ "unneeded var because I couldn't get it to work with just a single String 'name'").readNullable[String]
    ).apply(TableIndexColumnJson.apply _)
}