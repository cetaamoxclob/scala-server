package models

import play.api.libs.functional.syntax._
import play.api.libs.json.Reads._
import play.api.libs.json._

case class MenuItem(title: Option[String], href: Option[String], page: Option[String]) {
  val pageArtifact: Page = null
  def toHref(): String = {
    if (page.isDefined)
      "/page/Hello/"
    else
      href.toString
  }
}

case class MenuContent(title: String, items: Seq[MenuItem])

case class Menu(appTitle: String, content: Seq[MenuContent])

object Menu {
  implicit def menuItemReads: Reads[MenuItem] = (
    (JsPath \ "title").readNullable[String] and
      (JsPath \ "href").readNullable[String] and
      (JsPath \ "page").readNullable[String]
    ).apply(MenuItem.apply _)

  implicit def menuContentReads: Reads[MenuContent] = (
    (JsPath \ "title").read[String] and
      (JsPath \ "items").read[Seq[MenuItem]]
    ).apply(MenuContent.apply _)

  implicit def menuReads: Reads[Menu] = (
    (JsPath \ "appTitle").read[String] and
      (JsPath \ "content").read[Seq[MenuContent]]
    ).apply(Menu.apply _)

}

//  implicit def searchResultsWrites[T](implicit fmt: Writes[T]): Writes[SearchResults[T]] = new Writes[SearchResults[T]] {
//    def writes(ts: SearchResults[T]) = JsObject(Seq(
//      "page" -> JsNumber(ts.page),
//      "pageSize" -> JsNumber(ts.pageSize),
//      "total" -> JsNumber(ts.total),
//      "elements" -> JsArray(ts.elements.map(toJson(_)))
//    ))
//  }