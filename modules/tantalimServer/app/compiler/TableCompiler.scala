package compiler

import com.tantalim.models._
import models.src.{TableJoinColumnJson, TableJoinJson, TableColumnJson, TableJson}
import play.api.libs.json.{JsError, JsSuccess}
import services.{TableCache, ArtifactService}

import scala.collection.Map

trait TableCompiler extends ArtifactService with TableCache {

  def compileTable(name: String): DeepTable = {
    println("Compiling table " + name)
    val json = getArtifactContentAndParseJson(ArtifactType.Table, name)
    json.validate[TableJson] match {
      case JsSuccess(table, _) =>
        val columns = compileTableColumns(table.columns)
        new DeepTable(
          name,
          table.dbName.getOrElse(name),
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
        val columns = compileTableColumns(table.columns)
        new ShallowTable(
          name,
          table.dbName.getOrElse(name),
          primaryKey = if (table.primaryKey.isDefined) columns.get(table.primaryKey.get) else None,
          columns = columns
        )
      case JsError(err) =>
        throw new Exception("Failed to compile shallow table " + name + " due to the following error:" + err.toString)
    }
  }

  private def compileTableColumns(tableColumns: Seq[TableColumnJson]): scala.collection.immutable.Map[String, TableColumn] = {
    tableColumns.zipWithIndex.map{
      case (column, order) =>
        column.name -> compileTableColumn(column, order)}.toMap
  }

  private def compileTableColumn(column: TableColumnJson, order: Int): TableColumn = {
    new TableColumn(
      name = column.name,
      dbName = column.dbName.getOrElse(column.name),
      order = order,
      dataType = column.dataType.getOrElse("String"),
      fieldType = column.fieldType.getOrElse("text"),
      required = column.required.getOrElse(false),
      updateable = column.updateable.getOrElse(true),
      label = column.label.getOrElse(column.name),
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
        throw new Exception("Column `" + joinColumn.to + "` was not found when joining to table")
      ),
      from = if (joinColumn.from.isDefined) fromTable.get(joinColumn.from.get) else None,
      fromText = joinColumn.fromText
    )
  }

}
