package controllers

import play.api._
import play.api.mvc._

object Application extends Controller {

  def index = {
    desktop("Home")
  }

  def desktop(name: String) = Action {

    val page = new Page(name, name, None, None)
    val user = new User("12345", "trevorallred", "Trevor Allred")
    val menu = new Menu("Scala", List(
      new MenuContent("Manage", List(
        new MenuItem("Hello", None, Some("Hello"))
      ))
    ))

    Ok(views.html.desktop.index(page, menu, user))
  }

}