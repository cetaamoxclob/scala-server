package com.tantalim.models

import com.tantalim.util.TantalimException

abstract class Table {
  def name: String

  def dbName: String

  def module: Module

  def primaryKey: Option[TableColumn]

  def columns: Map[String, TableColumn]

  def getColumn(name: String) = columns.getOrElse(
    name,
    throw new TantalimException(f"failed to find column named `$name` in table `${this.name}`", s"found: ${this.columns.keys}")
  )
}

case class ShallowTable(
                         name: String,
                         dbName: String,
                         module: Module,
                         primaryKey: Option[TableColumn] = None,
                         columns: Map[String, TableColumn] = Map.empty
                         ) extends Table

case class DeepTable(
                      name: String,
                      dbName: String,
                      module: Module,
                      primaryKey: Option[TableColumn] = None,
                      columns: Map[String, TableColumn] = Map.empty,
                      joins: Map[String, TableJoin] = Map.empty,
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
