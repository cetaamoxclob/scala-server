package services

import models.src.{PageFieldJson, ModelJson, PageJson}
import models._
import play.api.libs.json.{JsError, JsSuccess}

object ArtifactCompiler {

  def compileMenu(name: String): Menu = {
    ArtifactService.getMenu match {
      case s: JsSuccess[Menu] => {
        val menu = s.get
        menu.copy(content = menu.content.map(content =>
          content.copy(items = content.items.map(item => {
            if (item.page.isDefined) {
              val pageName = item.page.get
              val json: PageJson = ArtifactService.getPage(pageName).getOrElse {
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

  def compilePage(name: String): Page = {
    val pageJson = ArtifactService.getArtifactContentAndParseJson(ArtifactType.Page, name)
    pageJson.validate[PageJson] match {
      case JsSuccess(page, _) => {
        val fields = page.fields.getOrElse(Seq.empty).map(compilePageField)
        new Page(
          name,
          page.title,
          page.icon,
          page.css,
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

  def compileModel(name: String): Model = {
    val modelJson = ArtifactService.getArtifactContentAndParseJson(ArtifactType.Model, name)
    modelJson.validate[ModelJson] match {
      case JsSuccess(model, _) => {
        new Model(name)
      }
      case JsError(err) => {
        throw new Exception("Failed to compile model " + name + " due to the following error:" + err.toString)
      }
    }

  }
}
