package models

abstract class Table {
  def name: String

  def dbName: String

  def columns: Map[String, TableColumn]
}

case class ShallowTable(
                         name: String,
                         dbName: String,
                         columns: Map[String, TableColumn]
                         ) extends Table

case class DeepTable(
                      name: String,
                      dbName: String,
                      primaryKey: Option[TableColumn],
                      columns: Map[String, TableColumn],
                      joins: Map[String, TableJoin]
                      ) extends Table

case class TableColumn(
                        name: String,
                        dbName: String,
                        dataType: String,
                        updateable: Boolean,
                        required: Boolean,
                        label: String,
                        fieldType: String
                        )

case class TableJoin(
                      name: String,
                      table: ShallowTable,
                      required: Boolean,
                      columns: Seq[TableJoinColumn]
                      )

case class TableJoinColumn(to: TableColumn,
                           from: Option[TableColumn],
                           fromText: Option[String])
