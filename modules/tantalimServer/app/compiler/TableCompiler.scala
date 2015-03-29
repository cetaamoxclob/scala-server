package compiler

import com.tantalim.models._
import compiler.src.{TableJoinColumnJson, TableJoinJson, TableColumnJson, TableJson}
import play.api.libs.json.{JsError, JsSuccess}
import services.{TableCache, ArtifactService}

import scala.collection.Map

trait TableCompiler extends ArtifactService with TableCache {

  def compileModule(moduleName: Option[String]): Module = {
    if (moduleName.isEmpty) {
      Module(
        "Default",
        Database("Default", None)
      )
    } else {
      val database = if (moduleName.get.toLowerCase.startsWith("tantalim")) {
        Database(moduleName.get, Some("tantalim_meta"))
      } else {
        Database(moduleName.get, None)
      }
      Module(
        "Default",
        database
      )
    }
  }

  def compileTable(name: String): DeepTable = {
    println("Compiling table " + name)
    val json = getArtifactContentAndParseJson(ArtifactType.Table, name)
    json.validate[TableJson] match {
      case JsSuccess(table, _) =>
        val columns = compileTableColumns(table)
        new DeepTable(
          name,
          table.dbName.getOrElse(name),
          module = compileModule(table.module),
          primaryKey = if (table.primaryKey.isDefined) columns.get(table.primaryKey.get) else None,
          columns = columns,
          joins = if (table.joins.isDefined) {
            table.joins.get.map(join => join.name -> compileTableJoin(columns, join)).toMap
          } else Map.empty,
          allowInsert = table.allowInsert.getOrElse(true),
          allowUpdate = table.allowUpdate.getOrElse(true),
          allowDelete = table.allowDelete.getOrElse(true)
        )
      case JsError(err) =>
        throw new Exception("Failed to compile table " + name + " due to the following error:" + err.toString)
    }
  }

  private def compileShallowTable(name: String): ShallowTable = {
    println("Compiling shallow table " + name)
    val json = getArtifactContentAndParseJson(ArtifactType.Table, name)
    json.validate[TableJson] match {
      case JsSuccess(table, _) =>
        val columns = compileTableColumns(table)
        new ShallowTable(
          name,
          table.dbName.getOrElse(name),
          module = compileModule(table.module),
          primaryKey = if (table.primaryKey.isDefined) columns.get(table.primaryKey.get) else None,
          columns = columns
        )
      case JsError(err) =>
        throw new Exception("Failed to compile shallow table " + name + " due to the following error:" + err.toString)
    }
  }

  private def compileTableColumns(table: TableJson): scala.collection.immutable.Map[String, TableColumn] = {
    table.columns.zipWithIndex.map {
      case (column, order) =>
        column.name -> compileTableColumn(table, column, order)
    }.toMap
  }

  private def compileTableColumn(table: TableJson, column: TableColumnJson, order: Int): TableColumn = {
    new TableColumn(
      name = column.name,
      dbName = column.dbName.getOrElse(column.name),
      order = order,
      dataType = compileDataType(column.dataType),
      fieldType = column.fieldType.getOrElse("text"),
      required = column.required.getOrElse(false),
      updateable = table.allowUpdate.getOrElse(true) && column.updateable.getOrElse(true),
      label = column.label.getOrElse(column.name),
      help = column.help,
      placeholder = column.placeholder
    )
  }

  private def compileDataType(value: Option[String]): DataType = {
    if (value.isEmpty) DataType.String
    else {
      val needle = value.get.toLowerCase
      DataType.values.find(t => t.toString.toLowerCase == needle).getOrElse(throw new Exception(s"${value.get} is not a valid DataType"))
    }
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
        throw new Exception("Column `" + joinColumn.to + "` was not found when joining to table")
      ),
      from = if (joinColumn.from.isDefined) fromTable.get(joinColumn.from.get) else None,
      fromText = joinColumn.fromText
    )
  }

}
