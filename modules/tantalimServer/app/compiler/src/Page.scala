package compiler.src

import play.api.libs.functional.syntax._
import play.api.libs.json.Reads._
import play.api.libs.json._

case class PageJson(title: Option[String],
                    icon: Option[String],
                    css: Option[String],
                    sections: Option[Seq[PageSectionJson]])

case class PageSectionJson(name: String,
                           title: Option[String],
                           model: Option[String],
                           viewMode: Option[String],
                           fields: Option[Seq[PageFieldJson]],
                           sections: Option[Seq[PageSectionJson]])

case class PageFieldJson(name: String,
                         showInFormView: Option[Boolean],
                         showInTableView: Option[Boolean],
                         showInNavigation: Option[Boolean],
                         label: Option[String],
                         fieldType: Option[String],
                         help: Option[String],
                         placeholder: Option[String],
                         filter: Option[String],
                         blurFunction: Option[String],
                         disabled: Option[Boolean],
                         searchable: Option[Boolean],
                         select: Option[PageFieldSelectJson],
                         links: Option[Seq[PageFieldLinkJson]]
                          )

case class PageFieldSelectJson(model: String,
                               sourceField: String,
                               targetID: Option[String],
                               fields: Option[Map[String, String]],
                               filter: Option[String]
                                )

case class PageFieldLinkJson(page: String, filter: String)

object PageJson {
  implicit def pageReads: Reads[PageJson] = (
    (JsPath \ "title").readNullable[String] and
      (JsPath \ "icon").readNullable[String] and
      (JsPath \ "css").readNullable[String] and
      (JsPath \ "sections").lazyReadNullable(Reads.seq[PageSectionJson](pageSectionReads))
    ).apply(PageJson.apply _)

  implicit def pageSectionReads: Reads[PageSectionJson] = (
    (JsPath \ "name").read[String] and
      (JsPath \ "title").readNullable[String] and
      (JsPath \ "model").readNullable[String] and
      (JsPath \ "viewMode").readNullable[String] and
      (JsPath \ "fields").readNullable[Seq[PageFieldJson]] and
      (JsPath \ "sections").lazyReadNullable(Reads.seq[PageSectionJson](pageSectionReads))
    ).apply(PageSectionJson.apply _)

  implicit def pageFieldReads: Reads[PageFieldJson] = (
    (JsPath \ "name").read[String] and
      (JsPath \ "showInFormView").readNullable[Boolean] and
      (JsPath \ "showInTableView").readNullable[Boolean] and
      (JsPath \ "showInNavigation").readNullable[Boolean] and
      (JsPath \ "label").readNullable[String] and
      (JsPath \ "fieldType").readNullable[String] and
      (JsPath \ "help").readNullable[String] and
      (JsPath \ "placeholder").readNullable[String] and
      (JsPath \ "filter").readNullable[String] and
      (JsPath \ "blurFunction").readNullable[String] and
      (JsPath \ "disabled").readNullable[Boolean] and
      (JsPath \ "searchable").readNullable[Boolean] and
      (JsPath \ "select").readNullable[PageFieldSelectJson] and
      (JsPath \ "links").readNullable[Seq[PageFieldLinkJson]]
    ).apply(PageFieldJson.apply _)

  implicit def selectReads: Reads[PageFieldSelectJson] = (
    (JsPath \ "model").read[String] and
      (JsPath \ "sourceField").read[String] and
      (JsPath \ "targetID").readNullable[String] and
      (JsPath \ "fields").readNullable[Map[String, String]] and
      (JsPath \ "filter").readNullable[String]
    ).apply(PageFieldSelectJson.apply _)

  implicit def linkReads: Reads[PageFieldLinkJson] = (
    (JsPath \ "page").read[String] and
      (JsPath \ "filter").read[String]
    ).apply(PageFieldLinkJson.apply _)

}
