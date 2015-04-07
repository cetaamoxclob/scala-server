package com.tantalim.models

import com.tantalim.util.TantalimException

abstract class Table {
  def name: String

  def dbName: String

  def module: Module

  def primaryKey: Option[TableColumn]

  def isMock = Table.isMock(name)

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
                        length: Option[Int] = None,
                        fieldType: FieldDisplay = FieldDisplay.Text
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

case class TableIndex(priority: Int, unique: Boolean, columns: Seq[TableColumn])

object Table {
  val Mock = "__MOCK_TABLE__"

  def isMock(name: String) = Table.Mock == name

  def createMock: DeepTable = {
    new DeepTable(
      Mock,
      dbName = null, // this should never be used
      module = null, // this should never be used
      columns = Map(
        TableColumn.createMock(DataType.String),
        TableColumn.createMock(DataType.Boolean),
        TableColumn.createMock(DataType.Date)
      ),
      allowInsert = false,
      allowUpdate = false,
      allowDelete = false
    )
  }
}

object TableColumn {
  def createMock(dataType: DataType): (String, TableColumn) = {
    val columnName = s"__MOCK_${dataType.toString.toUpperCase}__"

    (columnName, new TableColumn(
      name = columnName,
      dbName = null,
      order = 0,
      dataType = dataType,
      updateable = false,
      required = false,
      label = "Mock " + dataType.toString,
      fieldType = dataType match {
        case DataType.Boolean => FieldDisplay.Checkbox
        case DataType.Date => FieldDisplay.Date
        case DataType.DateTime => FieldDisplay.DateTime
        case _ => FieldDisplay.Text
      }
    ))
  }
}