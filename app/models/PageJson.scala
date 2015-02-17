package models

import play.api.libs.functional.syntax._
import play.api.libs.json.Reads._
import play.api.libs.json._

case class PageJson(title: String,
                    favicon: Option[String],
                    css: Option[String])

object PageJson {
  implicit def pageReads: Reads[PageJson] = (
    (JsPath \ "title").read[String] and
      (JsPath \ "favicon").readNullable[String] and
      (JsPath \ "css").readNullable[String]
    ).apply(PageJson.apply _)
}

