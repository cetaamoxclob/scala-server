package models.src

import play.api.libs.functional.syntax._
import play.api.libs.json.Reads._
import play.api.libs.json._

case class TableJson(dbName: String,
                     module: Option[String],
                     database: Option[String],
                     primaryKey: Option[String],
                     columns: Seq[TableColumnJson],
                     joins: Option[Seq[TableJoinJson]]
                      )

case class TableColumnJson(name: String,
                           dbName: String,
                           dataType: Option[String],
                           updateable: Option[Boolean],
                           required: Option[Boolean],
                           label: Option[String],
                           fieldType: Option[String]
                            )

case class TableJoinJson(name: String,
                         table: String,
                         required: Option[Boolean],
                         columns: Seq[TableJoinColumnJson])

case class TableJoinColumnJson(to: String,
                               from: Option[String],
                               fromText: Option[String])

object TableJson {
  implicit def tableReads: Reads[TableJson] = (
    (JsPath \ "dbName").read[String] and
      (JsPath \ "module").readNullable[String] and
      (JsPath \ "database").readNullable[String] and
      (JsPath \ "primaryKey").readNullable[String] and
      (JsPath \ "columns").read[Seq[TableColumnJson]] and
      (JsPath \ "joins").readNullable[Seq[TableJoinJson]]
    ).apply(TableJson.apply _)

  implicit def tableColumnReads: Reads[TableColumnJson] = (
    (JsPath \ "name").read[String] and
      (JsPath \ "dbName").read[String] and
      (JsPath \ "dataType").readNullable[String] and
      (JsPath \ "updateable").readNullable[Boolean] and
      (JsPath \ "required").readNullable[Boolean] and
      (JsPath \ "label").readNullable[String] and
      (JsPath \ "fieldType").readNullable[String]
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
}