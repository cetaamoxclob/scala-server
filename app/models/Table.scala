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
                      joins: Map[String, TableJoin],
                      allowInsert: Boolean,
                      allowUpdate: Boolean,
                      allowDelete: Boolean
                      ) extends Table

case class TableColumn(
                        name: String,
                        dbName: String,
                        dataType: String = "String",
                        updateable: Boolean = true,
                        required: Boolean = false,
                        label: String,
                        help: Option[String] = None,
                        placeholder: Option[String] = None,
                        fieldType: String = "text"
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
