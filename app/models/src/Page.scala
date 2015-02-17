package models.src

import play.api.libs.functional.syntax._
import play.api.libs.json.Reads._
import play.api.libs.json._

case class PageJson(title: String,
                    model: Option[String],
                    icon: Option[String],
                    css: Option[String],
                    fields: Option[Seq[PageFieldJson]],
                    children: Option[Seq[PageJson]])

case class PageFieldJson(name: String,
                         showInFormView: Option[Boolean],
                         showInTableView: Option[Boolean],
                         showInNavigation: Option[Boolean])

object PageJson {
  def empty = {
    new PageJson("", None, None, None, None, None)
  }

  implicit def pageReads: Reads[PageJson] = (
    (JsPath \ "title").read[String] and
      (JsPath \ "model").readNullable[String] and
      (JsPath \ "icon").readNullable[String] and
      (JsPath \ "css").readNullable[String] and
      (JsPath \ "fields").readNullable[Seq[PageFieldJson]] and
      (JsPath \ "children").lazyReadNullable(Reads.seq[PageJson](pageReads))
    ).apply(PageJson.apply _)

  implicit def pageFieldReads: Reads[PageFieldJson] = (
    (JsPath \ "name").read[String] and
      (JsPath \ "showInFormView").readNullable[Boolean] and
      (JsPath \ "showInTableView").readNullable[Boolean] and
      (JsPath \ "showInNavigation").readNullable[Boolean]
    ).apply(PageFieldJson.apply _)

}
