package compiler

import com.tantalim.models._
import com.tantalim.util.TantalimException
import compiler.src._
import play.api.libs.json.{JsError, JsSuccess}
import services.ArtifactService

import scala.collection.Seq

trait PageCompiler extends ArtifactService with ModelCompiler {

  def compilePage(name: String): Page = {
    println("Compiling page " + name)

    getPage(name) match {
      case JsSuccess(pageJson, _) =>
        val pageSections: Seq[PageSection] = pageJson.sections match {
          case Some(children) => children.map { section =>
            val modelName = section.model.getOrElse(section.name)
            val model = compileModel(modelName)
            compilePageSection(section, model)
          }
          case None => Seq.empty
        }
        if (pageSections.isEmpty) {
          throw new TantalimException(s"Page `$name` must have at least one section defined", "")
        }
        new Page(
          name,
          pageJson.title.getOrElse(name),
          pageJson.icon,
          pageJson.css,
          sections = pageSections
        )
      case JsError(err) =>
        throw new TantalimException(s"Failed to compile page $name", s"Due to the following error: $err")
    }
  }

  def compileShallowPage(name: String): ShallowPage = {
    println("Compiling shallow page " + name)
    val json = getArtifactContentAndParseJson(ArtifactType.Page, name)
    json.validate[PageJson] match {
      case JsSuccess(pageJson, _) =>
        new ShallowPage(
          name,
          pageJson.title.getOrElse(name),
          pageJson.icon
        )
      case JsError(err) =>
        throw new TantalimException("Failed to compile page " + name, "The JSON parse error was found at: " + err.toString)
    }
  }

  private def compilePageSection(sectionJson: PageSectionJson, model: Model, parentPage: Option[PageSection] = None): PageSection = {
    val fields = sectionJson.fields.getOrElse(Seq.empty).map { f =>
      val modelField = model.fields.getOrElse(f.name,
        throw new TantalimException(s"Failed to find field named `${f.name}` in Model `${model.name}` ",
          s"found these fields: ${model.fields.keys.mkString(", ")}")
      )
      val pageField = compilePageField(f, modelField)
      validateFieldSelect(pageField, model)
      pageField
    }
    val pageSection = new PageSection(
      sectionJson.name,
      sectionJson.title.getOrElse(""),
      viewMode = sectionJson.viewMode.getOrElse("form"),
      model = model,
      fields = fields,
      hasFormView = fields.exists(field => field.showInFormView),
      hasTableView = fields.exists(field => field.showInTableView),
      hasNavigation = fields.exists(field => field.showInNavigation),
      parent = parentPage,
      sections = null
    )
    pageSection.sections = sectionJson.sections match {
      case Some(childViews) => childViews.map { childView =>
        val childModelName = childView.model.getOrElse(childView.name)
        val childModel = model.children.get(childModelName).get
        compilePageSection(childView, childModel, Some(pageSection))
      }
      case None => Seq.empty
    }
    pageSection
  }

  private def validateFieldSelect(pageField: PageField, model: Model) = {
    if (pageField.select.isDefined) {
      val s = pageField.select.get
      val targetModel = try {
        compileModel(s.model)
      } catch {
        case e: TantalimException => throw new TantalimException("Failed to find model named " + s.model,
          s"Change the page definition, `${pageField.name}`.select.model to match a valid model")
      }
      if (targetModel.fields.get(s.sourceField).isEmpty)
        throw new TantalimException(
          s"Failed to find field named `${s.sourceField}` in Model `${targetModel.name}`",
          s"Change the page definition, `${pageField.name}`.select.sourceField to one of the following: ${targetModel.fields.keys.mkString(", ")}"
        )
      s.fields.foreach { case (from, to) =>
        // TODO Refactor all of these field gets
        if (model.fields.get(to).isEmpty)
          throw new TantalimException(
            s"Failed to find field named `$to` in Model `${model.name}`",
            s"Change the page definition, `${pageField.name}`.select.targetID to one of the following: ${model.fields.keys.mkString(", ")}"
          )
        if (model.fields.get(from).isEmpty)
          throw new TantalimException(
            s"Failed to find field named `$from` in Model `${model.name}`",
            s"Change the page definition, `${pageField.name}`.select.targetID to one of the following: ${model.fields.keys.mkString(", ")}"
          )
      }
    }

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
      filter = if (field.filter.isDefined) field.filter
      else {
        modelField.dataType match {
          case DataType.Date => Some("date:'yyyy-MM-dd'")
          case DataType.DateTime => Some("date:'yyyy-MM-dd HH:mm:ss'")
          case DataType.Integer => Some("number")
          case _ => None
        }
      },
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
      sourceField = selectJson.sourceField,
      targetID = selectJson.targetID,
      fields = selectJson.fields.getOrElse(Map.empty),
      filter = selectJson.filter
    )
  }

  private def compileFieldLink(linkJson: PageFieldLinkJson): PageFieldLink = {
    new PageFieldLink(
      page = compileShallowPage(linkJson.page),
      filter = linkJson.filter
    )
  }

}
