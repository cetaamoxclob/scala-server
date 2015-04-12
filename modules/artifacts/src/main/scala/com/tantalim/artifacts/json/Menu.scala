package com.tantalim.artifacts.json

import play.api.libs.functional.syntax._
import play.api.libs.json.Reads._
import play.api.libs.json._

case class MenuJson(appTitle: String, content: Seq[MenuContentJson])

case class MenuContentJson(title: String, items: Seq[MenuItemJson])

case class MenuItemJson(title: Option[String], href: Option[String], page: Option[String], icon: Option[String])

object MenuJson {
  implicit def menuItemReads: Reads[MenuItemJson] = (
    (JsPath \ "title").readNullable[String] and
      (JsPath \ "href").readNullable[String] and
      (JsPath \ "page").readNullable[String] and
      (JsPath \ "icon").readNullable[String]
    ).apply(MenuItemJson.apply _)

  implicit def menuContentReads: Reads[MenuContentJson] = (
    (JsPath \ "title").read[String] and
      (JsPath \ "items").read[Seq[MenuItemJson]]
    ).apply(MenuContentJson.apply _)

  implicit def menuReads: Reads[MenuJson] = (
    (JsPath \ "appTitle").read[String] and
      (JsPath \ "content").read[Seq[MenuContentJson]]
    ).apply(MenuJson.apply _)

}
