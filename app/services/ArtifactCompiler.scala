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
    println("Compiling page " + name)
    val json = getArtifactContentAndParseJson(ArtifactType.Page, name)
    json.validate[PageJson] match {
      case JsSuccess(pageJson, _) => {
        val page = pageJson.copy(name = Option(name))
        val modelName: String = page.model.getOrElse(page.name.getOrElse {
          throw new Exception("Neither the model nor the page has a name: " + page.toString)
        })
        val model = compileModel(modelName)
        compilePageView(page, model)
      }
      case JsError(err) => {
        throw new Exception("Failed to compile page " + name + " due to the following error:" + err.toString)
      }
    }
  }

  private def findField(fieldName: String, model: Model): Option[ModelField] = {
    model.fields.get(fieldName) match {
      case Some(field) => Option(field)
      case None => findField(fieldName, model.steps.values.toSeq)
    }
  }

  private def findField(fieldName: String, steps: Seq[ModelStep]): Option[ModelField] = {
    steps.map { step =>
      findField(fieldName, step)
    }.flatMap(fields => fields).headOption
  }

  private def findField(fieldName: String, step: ModelStep): Option[ModelField] = {
    step.fields.get(fieldName) match {
      case Some(field) => Option(field)
      case None => findField(fieldName, step.steps.values.toSeq)
    }
  }

  private def compilePageView(pageJson: PageJson, model: Model): Page = {
    val fields = pageJson.fields.getOrElse(Seq.empty).map { f =>
      val modelField = findField(f.name, model).getOrElse {
        throw new Exception(f"Failed to find field named `${f.name}` in Model `${model.name}` but found these: \n${model.fields.keys} \n${model.steps.values}")
      }
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
      hasFormView = fields.find(field => field.showInFormView).isDefined,
      hasTableView = fields.find(field => field.showInTableView).isDefined,
      hasNavigation = fields.find(field => field.showInNavigation).isDefined,
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
    new PageField(
      name = field.name,
      fieldType = field.fieldType.getOrElse("text"), // Need to inherit from basisColumn
      required = modelField.required,
      disabled = field.disabled.getOrElse(false), // Need to inherit from basisColumn
      label = field.label.getOrElse(modelField.name), // Need to inherit from basisColumn
      showInFormView = field.showInFormView.getOrElse(true),
      showInTableView = field.showInTableView.getOrElse(true),
      showInNavigation = field.showInNavigation.getOrElse(false),
      placeholder = field.placeholder,
      help = field.help,
      filter = field.filter,
      blurFunction = field.blurFunction,
      select = field.select match {
        case Some(s) => Option(compileFieldSelect(s))
        case None => None
      },
      links = None // TODO
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

  def compileModel(name: String): Model = {
    println("Compiling model " + name)
    val json = getArtifactContentAndParseJson(ArtifactType.Model, name)
    json.validate[ModelJson] match {
      case JsSuccess(modelJson, _) => {
        compileModelView(modelJson.copy(name = Option(name)))
      }
      case JsError(err) => {
        throw new Exception("Failed to compile model " + name + " due to the following error:" + err.toString)
      }
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
      steps = compileSteps(basisTable.joins, model.steps),
      orderBy = compileOrderBy(model.orderBy)
    )
  }

  private def compileSteps(fromTableJoins: Map[String, TableJoin], steps: Option[Seq[ModelStepJson]]): collection.immutable.Map[Int, ModelStep] = {
    if (steps.isEmpty) Map.empty
    else {
      steps.get.zipWithIndex.map {
        case (step, counter) => {
          val toTable = fromTableJoins.getOrElse(
            step.join,
            throw new Exception(f"Failed to find table named `${step.join}` in join clause")
          ).table
          val fields = step.fields.map(field => {
            field.name -> compileModelField(field, toTable.columns.get(field.basisColumn).getOrElse {
              throw new Exception("field.name ${field.name}")
            })
          }).toMap
          counter -> new ModelStep(
            table = toTable,
            step.required.getOrElse(true),
            fields = fields,
            steps = Map.empty
          )
        }
      }.toMap
    }
  }

  private def compileOrderBy(orderBy: Option[Seq[OrderByJson]]): Seq[ModelOrderBy] = {
    if (orderBy.isEmpty) Seq.empty
    else {
      orderBy.get.map(o => new ModelOrderBy(o.fieldName, o.ascending))
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
