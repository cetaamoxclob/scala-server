package com.tantalim.models

abstract class Table {
  def name: String

  def dbName: String

  def primaryKey: Option[TableColumn]

  def columns: Map[String, TableColumn]

  def getColumn(name: String) = columns.getOrElse(
    name,
    throw new Exception(f"failed to find column named `$name` in table `${this.name}` but found: ${this.columns.keys}")
  )
}

case class ShallowTable(
                         name: String,
                         dbName: String,
                         primaryKey: Option[TableColumn] = None,
                         columns: Map[String, TableColumn]
                         ) extends Table

case class DeepTable(
                      name: String,
                      dbName: String,
                      primaryKey: Option[TableColumn],
                      columns: Map[String, TableColumn],
                      joins: Map[String, TableJoin],
                      allowInsert: Boolean = true,
                      allowUpdate: Boolean = true,
                      allowDelete: Boolean = true
                      ) extends Table

case class TableColumn(
                        name: String,
                        dbName: String,
                        order: Int,
                        dataType: DataType = DataType.String,
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
