package controllers

import com.google.common.base.Charsets
import com.google.common.io.Files
import play.api.Play.current
import play.api._
import play.api.db._
import play.api.libs.json.Json._
import play.api.libs.json.Writes._
import play.api.libs.json._
import play.api.mvc._

object Application extends Controller {

  def index = {
    desktop("Home")
  }

  def readData(name: String) = {

    Ok(toJson(Map("status" -> "OK", "message" -> ("Hello World" + name))))
  }

  def desktop(name: String) = Action {
//    val ds = DB.getDataSource()
    var outString = "Number is "
    val conn = DB.getConnection()
    try {
      val stmt = conn.createStatement
      val rs = stmt.executeQuery("SELECT 9 as testkey ")
      while (rs.next()) {
        outString += rs.getString("testkey")
      }
    } finally {
      conn.close()
    }
    println(outString)

    val menuContent = Files.toString(Play.getFile("src/menus/Default.json"), Charsets.UTF_8)
    val menuJson = Json.parse(menuContent)

    val pageContent = Files.toString(Play.getFile("src/pages/" + name + ".json"), Charsets.UTF_8)
    val page = new Page(name, name, None, None)
    val user = new User("12345", "trevorallred", "Trevor Allred")

    menuJson.validate[Menu] match {
      case s: JsSuccess[Menu] => {
        val menu: Menu = s.get

        Ok(views.html.desktop.index(page, menu, user))
      }
      case e: JsError => {
        println(e)
        val menu = new Menu(
          "title", ???
        )
        Ok(views.html.desktop.index(page, menu, user))
      }
    }

  }

}