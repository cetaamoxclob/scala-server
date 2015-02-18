package services

import models.src._
import models._
import play.api.libs.json.{JsError, JsSuccess}
import scala.collection._

trait ArtifactCompiler extends ArtifactService with TableCache {

  def compileMenu(name: String): Menu = {
    getMenu match {
      case s: JsSuccess[Menu] => {
        val menu = s.get
        menu.copy(content = menu.content.map(content =>
          content.copy(items = content.items.map(item => {
            if (item.page.isDefined) {
              val pageName = item.page.get
              val json: PageJson = getPage(pageName).getOrElse {
                PageJson.empty.copy(title = "Error: " + pageName)
              }
              item.copy(
                title = Option(item.title.getOrElse(json.title)),
                href = Option(item.href.getOrElse("/page/" + pageName + "/"))
              )
            } else {
              item
            }
          }
          ))
        ))
      }
      case err: JsError => {
        throw new Exception("Failed to compile menu " + name + " due to the following error:" + err.toString)
      }
    }
  }

  def compilePage(name: String): Page = {
    val json = getArtifactContentAndParseJson(ArtifactType.Page, name)
    json.validate[PageJson] match {
      case JsSuccess(page, _) => {
        val fields = page.fields.getOrElse(Seq.empty).map(compilePageField)
        new Page(
          name,
          page.title,
          page.icon,
          page.css,
          viewMode = page.viewMode.getOrElse("form"),
          model = compileModel(page.model.getOrElse(name)),
          fields = fields,
          hasFormView = fields.find(field => field.showInFormView).isDefined,
          hasTableView = fields.find(field => field.showInTableView).isDefined,
          hasNavigation = fields.find(field => field.showInNavigation).isDefined,
          children = Seq.empty)
      }
      case JsError(err) => {
        throw new Exception("Failed to compile page " + name + " due to the following error:" + err.toString)
      }
    }
  }

  private def compilePageField(field: PageFieldJson): PageField = {
    new PageField(
      name = field.name,
      fieldType = "text",
      required = false,
      disabled = false,
      label = field.name,
      showInFormView = field.showInFormView.getOrElse(true),
      showInTableView = field.showInTableView.getOrElse(true),
      showInNavigation = field.showInNavigation.getOrElse(false),
      placeholder = None,
      help = None,
      filter = None, // field.filter
      blurFunction = None,
      select = None,
      links = None
    )
  }

  def compileModel(name: String): Model = {
    val json = getArtifactContentAndParseJson(ArtifactType.Model, name)
    json.validate[ModelJson] match {
      case JsSuccess(model, _) => {
        val basisTable = getTableFromCache(model.basisTable).getOrElse{
          val newTable = compileTable(name)
          addTableToCache(name, newTable)
          newTable
        }
        val instanceIdField = if (basisTable.primaryKey.isDefined) {
          model.fields.find(f =>
            f.basisColumn == basisTable.primaryKey.get.name // && f.step
          )
        } else None
        new Model(
          name,
          basisTable,
          instanceID = if (instanceIdField.isDefined) Option(instanceIdField.get.name) else None,
          fields = model.fields.map(f => {
            val basisColumn = basisTable.columns.getOrElse(f.basisColumn, throw new Exception("failed to find column in table"))
            f.name -> compileModelField(f, basisColumn)
          }).toMap
        )
      }
      case JsError(err) => {
        throw new Exception("Failed to compile model " + name + " due to the following error:" + err.toString)
      }
    }
  }

  private def compileModelField(field: ModelFieldJson, basisColumn: TableColumn): ModelField = {
    new ModelField(
      name = field.name,
      dbName = basisColumn.dbName,
      dataType = basisColumn.dataType,
      required = field.required.getOrElse(basisColumn.required),
      updateable = field.updateable.getOrElse(basisColumn.updateable)
    )
  }

  private def compileTable(name: String): DeepTable = {
    println("Compiling table " + name)
    val json = getArtifactContentAndParseJson(ArtifactType.Table, name)
    json.validate[TableJson] match {
      case JsSuccess(table, _) => {
        val columns = table.columns.map(column => column.name -> compileTableColumn(column)).toMap
        new DeepTable(
          name,
          table.dbName,
          primaryKey = if (table.primaryKey.isDefined) columns.get(table.primaryKey.get) else None,
          columns = columns,
          joins = if (table.joins.isDefined) {
            table.joins.get.map(join => join.name -> compileTableJoin(columns, join)).toMap
          } else Map.empty
        )
      }
      case JsError(err) => {
        throw new Exception("Failed to compile table " + name + " due to the following error:" + err.toString)
      }
    }
  }

  private def compileShallowTable(name: String): ShallowTable = {
    println("Compiling shallow table " + name)
    val json = getArtifactContentAndParseJson(ArtifactType.Table, name)
    json.validate[TableJson] match {
      case JsSuccess(table, _) => {
        val columns = table.columns.map(column => column.name -> compileTableColumn(column)).toMap
        new ShallowTable(
          name,
          table.dbName,
          columns = columns
        )
      }
      case JsError(err) => {
        throw new Exception("Failed to compile shallow table " + name + " due to the following error:" + err.toString)
      }
    }
  }

  private def compileTableColumn(column: TableColumnJson): TableColumn = {
    new TableColumn(
      name = column.name,
      dbName = column.dbName,
      dataType = column.dataType.getOrElse("String"),
      fieldType = column.fieldType.getOrElse("text"),
      required = column.required.getOrElse(false),
      updateable = column.updateable.getOrElse(true),
      label = column.label.getOrElse(column.name)
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
      to = toTable.get(joinColumn.to).getOrElse {
        throw new Exception("Column `" + joinColumn.to + "` was not found when joining to table")
      },
      from = if (joinColumn.from.isDefined) {
        fromTable.get(joinColumn.from.get)
      } else None,
      fromText = joinColumn.fromText
    )
  }

}

class ArtifactCompilerService extends ArtifactCompiler
