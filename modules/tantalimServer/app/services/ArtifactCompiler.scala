package services

import com.tantalim.models._
import models.src._
import play.api.libs.json.{JsError, JsSuccess}
import scala.collection._

trait ArtifactCompiler extends ArtifactService with TableCache {

  def compileMenu(name: String): Menu = {
    println("Compiling menu " + name)

    getMenu(name) match {
      case JsSuccess(menu, _) =>
        new Menu(
          menu.appTitle,
          menu.content.map(content => compileMenuContent(content))
        )
      case err: JsError =>
        throw new Exception("Failed to compile menu " + name + " due to the following error:" + err.toString)
    }
  }

  private def compileMenuContent(contentJson: MenuContentJson): MenuContent = {
    new MenuContent(
      contentJson.title,
      contentJson.items.map(item => compileMenuItem(item))
    )
  }

  private def compileMenuItem(itemJson: MenuItemJson): MenuItem = {
    if (itemJson.page.isDefined) {
      val page = compileShallowPage(itemJson.page.get)
      new MenuItem(
        itemJson.title.getOrElse(page.title),
        "/page/" + itemJson.page.get + "/",
        itemJson.icon.map(icon => Icon(icon)).orElse(page.icon.map(icon => Icon(icon)))
      )
    } else {
      new MenuItem(
        itemJson.title.get, // title is required if page is missing
        itemJson.href.get, // href is required if page is missing
        itemJson.icon.map(icon => Icon(icon))
      )
    }
  }

  def compilePage(name: String): Page = {
    println("Compiling page " + name)

    getPage(name) match {
      case JsSuccess(pageJson, _) =>
        val page = pageJson.copy(name = Option(name))
        val modelName: String = page.model.getOrElse(page.name.getOrElse {
          throw new Exception("Neither the model nor the page has a name: " + page.toString)
        })
        val model = compileModel(modelName)
        compilePageView(page, model)
      case JsError(err) =>
        throw new Exception("Failed to compile page " + name + " due to the following error:" + err.toString)
    }
  }

  def compileShallowPage(name: String): ShallowPage = {
    println("Compiling shallow page " + name)
    val json = getArtifactContentAndParseJson(ArtifactType.Page, name)
    json.validate[PageJson] match {
      case JsSuccess(pageJson, _) =>
        new ShallowPage(
          name,
          pageJson.title,
          pageJson.icon
        )
      case JsError(err) =>
        throw new Exception("Failed to compile page " + name + " due to the following error:" + err.toString)
    }
  }

  private def compilePageView(pageJson: PageJson, model: Model, parentPage: Option[Page] = None): Page = {
    val fields = pageJson.fields.getOrElse(Seq.empty).map { f =>
      val modelField = model.fields.getOrElse(f.name,
        throw new Exception(f"Failed to find field named `${f.name}` in Model `${model.name}` " +
          f"but found these: \n${model.fields.keys} \n${model.steps.values}")
      )
      compilePageField(f, modelField)
    }
    new Page(
      pageJson.name.get,
      pageJson.title,
      pageJson.icon,
      pageJson.css,
      viewMode = pageJson.viewMode.getOrElse("form"),
      model = model,
      fields = fields,
      hasFormView = fields.exists(field => field.showInFormView),
      hasTableView = fields.exists(field => field.showInTableView),
      hasNavigation = fields.exists(field => field.showInNavigation),
      parentPage = parentPage,
      children = pageJson.children match {
        case Some(childViews) => childViews.map { childView =>
          val childModelName = childView.model.getOrElse(childView.name.get)
          val childModel = model.children.get(childModelName).get
          compilePageView(childView, childModel)
        }
        case None => Seq.empty
      }
    )
  }

  private def compilePageField(field: PageFieldJson, modelField: ModelField): PageField = {
    val column = modelField.basisColumn
    new PageField(
      name = field.name,
      modelField = modelField,
      fieldType = field.fieldType.getOrElse(column.fieldType),
      required = modelField.required,
      disabled = field.disabled.getOrElse(!column.updateable),
      label = field.label.getOrElse(column.label),
      searchable = true,
      showInFormView = field.showInFormView.getOrElse(true),
      showInTableView = field.showInTableView.getOrElse(true),
      showInNavigation = field.showInNavigation.getOrElse(false),
      placeholder = if (field.placeholder.isDefined) field.placeholder else column.placeholder,
      help = if (field.help.isDefined) field.help else column.help,
      filter = field.filter,
      blurFunction = field.blurFunction,
      select = field.select match {
        case Some(s) => Some(compileFieldSelect(s))
        case None => None
      },
      links = field.links match {
        case Some(s) => s.map(link => compileFieldLink(link))
        case None => Seq.empty
      }
    )
  }

  private def compileFieldSelect(selectJson: PageFieldSelectJson): PageFieldSelect = {
    new PageFieldSelect(
      model = selectJson.model,
      sourceValue = selectJson.sourceValue,
      targetID = selectJson.targetID,
      where = selectJson.where,
      otherMappings = selectJson.otherMappings
    )
  }

  private def compileFieldLink(linkJson: PageFieldLinkJson): PageFieldLink = {
    new PageFieldLink(
      page = compileShallowPage(linkJson.page),
      filter = linkJson.filter
    )
  }

  def compileModel(name: String): Model = {
    println("Compiling model " + name)
    val json = getArtifactContentAndParseJson(ArtifactType.Model, name)
    json.validate[ModelJson] match {
      case JsSuccess(modelJson, _) =>
        compileModelView(modelJson.copy(name = Option(name)))
      case JsError(err) =>
        throw new Exception("Failed to compile model " + name + " due to the following error:" + err.toString)
    }
  }

  private def compileModelView(model: ModelJson): Model = {
    println("Compiling model view " + model.name.getOrElse("unknown"))
    val basisTable = getTableFromCache(model.basisTable).getOrElse {
      val newTable = compileTable(model.basisTable)
      addTableToCache(model.name.get, newTable)
      newTable
    }
    val instanceIdField = if (basisTable.primaryKey.isDefined) {
      model.fields.find(f =>
        f.basisColumn == basisTable.primaryKey.get.name // && f.step
      )
    } else None
    new Model(
      model.name.get,
      basisTable,
      model.limit.getOrElse(0),
      instanceID = if (instanceIdField.isDefined) Option(instanceIdField.get.name) else None,
      fields = model.fields.map(f => {
        //        if (f.step )
        val basisColumn = basisTable.columns.getOrElse(
          f.basisColumn,
          throw new Exception(f"failed to find column for `${f.name}` named `${f.basisColumn}` in table `${basisTable.name}` but found: ${basisTable.columns.keys}")
        )
        f.name -> compileModelField(f, basisColumn)
      }).toMap,
      children = model.children match {
        case Some(modelChildren) => modelChildren.map(childModel => {
          childModel.name.get -> compileModelView(childModel)
        }).toMap
        case None => Map.empty
      },
      parentLink = model.parentLink,
      steps = compileSteps(basisTable.joins, model.steps),
      orderBy = compileOrderBy(model.orderBy),
      allowInsert = model.allowInsert.getOrElse(basisTable.allowInsert),
      allowUpdate = model.allowUpdate.getOrElse(basisTable.allowUpdate),
      allowDelete = model.allowDelete.getOrElse(basisTable.allowDelete)
    )
  }

  private def compileSteps(fromTableJoins: Map[String, TableJoin], steps: Option[Seq[ModelStepJson]]): collection.immutable.Map[Int, ModelStep] = {
    if (steps.isEmpty) Map.empty
    else {
      steps.get.zipWithIndex.map {
        case (step, counter) =>
          val toTable = fromTableJoins.getOrElse(
            step.join,
            throw new Exception(f"Failed to find table named `${step.join}` in join clause")
          ).table
          counter -> new ModelStep(
            table = toTable,
            step.required.getOrElse(true),
            steps = Map.empty
          )
      }.toMap
    }
  }

  private def compileOrderBy(orderBy: Option[Seq[ModelOrderBy]]): Seq[ModelOrderBy] = {
    if (orderBy.isEmpty) Seq.empty
    else {
      orderBy.get.map(o => new ModelOrderBy(o.fieldName, o.ascending))
    }
  }

  private def compileModelField(field: ModelFieldJson, basisColumn: TableColumn): ModelField = {
    new ModelField(
      name = field.name,
      basisColumn = basisColumn,
      step = field.step,
      required = field.required.getOrElse(basisColumn.required),
      updateable = field.updateable.getOrElse(basisColumn.updateable)
    )
  }

  private def compileTable(name: String): DeepTable = {
    println("Compiling table " + name)
    val json = getArtifactContentAndParseJson(ArtifactType.Table, name)
    json.validate[TableJson] match {
      case JsSuccess(table, _) =>
        val columns = table.columns.map(column => column.name -> compileTableColumn(column)).toMap
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
        val columns = table.columns.map(column => column.name -> compileTableColumn(column)).toMap
        new ShallowTable(
          name,
          table.dbName.getOrElse(name),
          columns = columns
        )
      case JsError(err) =>
        throw new Exception("Failed to compile shallow table " + name + " due to the following error:" + err.toString)
    }
  }

  private def compileTableColumn(column: TableColumnJson): TableColumn = {
    new TableColumn(
      name = column.name,
      dbName = column.dbName.getOrElse(column.name),
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
        joinColumn.to,
        throw new Exception("Column `" + joinColumn.to + "` was not found when joining to table")
      ),
      from = if (joinColumn.from.isDefined) fromTable.get(joinColumn.from.get) else None,
      fromText = joinColumn.fromText
    )
  }

}

class ArtifactCompilerService extends ArtifactCompiler
