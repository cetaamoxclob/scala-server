package compiler

import com.tantalim.models._
import models.src.{PageFieldLinkJson, PageFieldSelectJson, PageFieldJson, PageJson}
import play.api.libs.json.{JsError, JsSuccess}
import services.ArtifactService

import scala.collection.Seq

trait PageCompiler extends ArtifactService with ModelCompiler {

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

}
