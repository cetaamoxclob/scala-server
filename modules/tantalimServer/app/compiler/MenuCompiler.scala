package compiler

import com.tantalim.models.{Icon, MenuItem, MenuContent, Menu}
import models.src.{MenuItemJson, MenuContentJson}
import play.api.libs.json.{JsError, JsSuccess}
import services.ArtifactService

trait MenuCompiler extends ArtifactService with PageCompiler {
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


}
