package com.tantalim.artifacts.compiler

import com.tantalim.artifacts.json.{MenuJson, MenuContentJson, MenuItemJson}
import com.tantalim.artifacts.{ArtifactService, MissingArtifactException}
import com.tantalim.models._
import com.tantalim.util.TantalimException
import play.api.libs.json.{JsResult, JsError, JsSuccess}

trait MenuCompiler extends ArtifactService with PageCompiler {

  private def getMenu(name: String): JsResult[MenuJson] = {
    val artifactJson = getArtifactContentAndParseJson(MenuCompiler.artifactName, name)
    artifactJson.validate[MenuJson]
  }

  def compileMenu(name: String): Menu = {
    println("Compiling menu " + name)

    getMenu(name) match {
      case JsSuccess(menu, _) =>
        new Menu(
          menu.appTitle,
          menu.content.map(content => compileMenuContent(content))
        )
      case err: JsError =>
        throw new TantalimException("Failed to compile menu " + name, "The following error:" + err.toString)
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
      val page: ShallowPage = try {
        compileShallowPage(itemJson.page.get)
      } catch {
        case e: MissingArtifactException => ShallowPage(
          name = itemJson.page.get,
          title = s"Missing (${itemJson.page.get})"
        )
        case e: TantalimException => ShallowPage(
          name = itemJson.page.get,
          title = s"Error (${itemJson.page.get})"
        )
      }
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


}

object MenuCompiler {
  val artifactName = "menus"
}