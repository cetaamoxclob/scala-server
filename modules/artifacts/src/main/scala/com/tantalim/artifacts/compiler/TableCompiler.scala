package com.tantalim.artifacts.compiler

import com.tantalim.artifacts.{ArtifactService, TableCache}
import com.tantalim.artifacts.json._
import com.tantalim.models._
import com.tantalim.util.TantalimException
import play.api.libs.json.{JsError, JsSuccess}

import scala.collection.Map

trait TableCompiler extends ArtifactService with TableCache {

  def compileModule(moduleName: Option[String]): Module = {
    // TODO
    if (moduleName.isEmpty) {
      Module(
        Module.default,
        Database("Default", None)
      )
    } else {
      Module(
        moduleName.get,
        Database(moduleName.get,
          if (moduleName.get.toLowerCase.startsWith("tantalim")) Some("tantalim_meta")
          else None
        )
      )
    }
  }

  def compileTable(name: String): DeepTable = {
    println("Compiling table " + name)
    val json = getArtifactContentAndParseJson(ArtifactType.Table, name)
    json.validate[TableJson] match {
      case JsSuccess(table, _) =>
        compileDeepTable(name, table)
      case JsError(err) =>
        throw new Exception("Failed to compile table " + name + " due to the following error:" + err.toString)
    }
  }

  private def compileShallowTable(name: String): ShallowTable = {
    println("Compiling shallow table " + name)
    val json = getArtifactContentAndParseJson(ArtifactType.Table, name)
    json.validate[TableJson] match {
      case JsSuccess(table, _) =>
        compileShallowTable(name, table)
      case JsError(err) =>
        throw new Exception("Failed to compile shallow table " + name + " due to the following error:" + err.toString)
    }
  }

  private def compileShallowTable(name: String, table: TableJson): ShallowTable = {
    val columns = compileTableColumns(table)
    new ShallowTable(
      name,
      table.dbName.getOrElse(name),
      module = compileModule(table.module),
      primaryKey = if (table.primaryKey.isDefined) columns.get(table.primaryKey.get) else None,
      columns = columns,
      indexes = if (table.indexes.isDefined) {
        table.indexes.get.map(index => compileTableIndex(columns, index))
      } else Seq.empty
    )
  }

  private def compileDeepTable(name: String, table: TableJson): DeepTable = {
    val shallowTable = compileShallowTable(name, table)
    new DeepTable(
      name = shallowTable.name,
      dbName = shallowTable.dbName,
      module = shallowTable.module,
      primaryKey = shallowTable.primaryKey,
      columns = shallowTable.columns,
      joins = if (table.joins.isDefined) {
        table.joins.get.map(join => join.name -> compileTableJoin(shallowTable.columns, join)).toMap
      } else Map.empty,
      indexes = shallowTable.indexes,
      allowInsert = table.allowInsert.getOrElse(true),
      allowUpdate = table.allowUpdate.getOrElse(true),
      allowDelete = table.allowDelete.getOrElse(true)
    )
  }

  private def compileTableColumns(table: TableJson): scala.collection.immutable.Map[String, TableColumn] = {
    table.columns.zipWithIndex.map {
      case (column, order) =>
        column.name -> compileTableColumn(table, column, order)
    }.toMap
  }

  private def compileTableColumn(table: TableJson, column: TableColumnJson, order: Int): TableColumn = {
    val dataType: DataType = TableCompiler.compileDataType(column.dataType)
    new TableColumn(
      name = column.name,
      dbName = column.dbName.getOrElse(column.name),
      order = order,
      dataType = dataType,
      fieldType = if (column.fieldType.isEmpty) {
        val display = dataType match {
          case DataType.Boolean => FieldDisplay.Checkbox
          case _ => if (column.length.isDefined && column.length.get > 100) FieldDisplay.Textarea
          else FieldDisplay.Text
        }
        display
      } else TableCompiler.compileFieldDisplay(column.fieldType.get),
      required = column.required.getOrElse(false),
      updateable = table.allowUpdate.getOrElse(true) && column.updateable.getOrElse(true),
      label = column.label.getOrElse(column.name),
      length = column.length,
      help = column.help,
      placeholder = column.placeholder
    )
  }

  private def compileTableJoin(fromColumns: Map[String, TableColumn], join: TableJoinJson): TableJoin = {
    val toTable = compileShallowTable(join.table)
    new TableJoin(
      name = join.name,
      table = toTable,
      required = join.required.getOrElse(false),
      columns = join.columns.map(c => compileTableJoinColumn(fromColumns, toTable.columns, c))
    )
  }

  private def compileTableJoinColumn(fromTable: Map[String, TableColumn], toTable: Map[String, TableColumn], joinColumn: TableJoinColumnJson): TableJoinColumn = {
    new TableJoinColumn(
      to = toTable.getOrElse(
        joinColumn.to, // TODO Default toColumn - Assume we're joining to the primaryKey of the target table
        throw new TantalimException("Column `" + joinColumn.to + "` was not found when joining to table", "")
      ),
      from = if (joinColumn.from.isDefined) fromTable.get(joinColumn.from.get) else None,
      fromText = joinColumn.fromText
    )
  }

  private def compileTableIndex(columns: Map[String, TableColumn], index: TableIndexJson): TableIndex = {
    TableIndex(
      index.priority,
      index.unique.getOrElse(false),
      index.columns.map(indexColumn => columns.getOrElse(indexColumn.name,
        throw new TantalimException("Missing Index Column " + indexColumn.name,
          "Existing columns: " + columns.keys.mkString(", ")))
      )
    )
  }
}

object TableCompiler {
  private def compileDataType(value: Option[String]): DataType = {
    if (value.isEmpty || value.get.trim.isEmpty) DataType.String
    else {
      val needle = value.get.toLowerCase
      DataType.values.find(t => t.toString.toLowerCase == needle).getOrElse(throw new Exception(s"${value.get} is not a valid DataType"))
    }
  }

  def compileFieldDisplay(value: String): FieldDisplay = {
    val needle = value.toLowerCase
    FieldDisplay.values.find(t => t.toString.toLowerCase == needle).getOrElse(throw new Exception(s"$value is not a valid FieldDisplay"))
  }
}