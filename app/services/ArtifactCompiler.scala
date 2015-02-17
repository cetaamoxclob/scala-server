package services

import models.src.{ModelJson, PageJson}
import models.{Model, ArtifactType, Page, Menu}
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

  def compilePage(name: String): Page = {
    val pageJson = ArtifactService.getArtifactContentAndParseJson(ArtifactType.Page, name)
    pageJson.validate[PageJson] match {
      case JsSuccess(page, _) => {
        new Page(
          name,
          page.title,
          page.icon,
          page.css,
          model = compileModel(page.model.getOrElse(name)),
          fields = Seq.empty,
          hasFormView = false,
          hasTableView = false,
          hasNavigation = false,
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
