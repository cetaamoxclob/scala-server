package core.compiler.src

import play.api.libs.functional.syntax._
import play.api.libs.json.Reads._
import play.api.libs.json._

case class PageJson(title: Option[String],
                    icon: Option[String],
                    css: Option[String],
                    sections: Option[Seq[PageSectionJson]]
                     )

case class PageSectionJson(name: String,
                           title: Option[String],
                           model: Option[String],
                           viewMode: Option[String],
                           fields: Option[Seq[PageFieldJson]],
                           buttons: Option[Seq[PageButtonJson]],
                           sections: Option[Seq[PageSectionJson]]
                            )

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
                         selectModel: Option[String],
                         selectSourceField: Option[String],
                         selectTargetID: Option[String],
                         // selectFields: Option[Map[String, String]],
                         selectFilter: Option[String],
                         links: Option[Seq[PageFieldLinkJson]]
                          )

case class PageFieldLinkJson(page: String, filter: String)

case class PageButtonJson(label: String, function: String)

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
      (JsPath \ "buttons").readNullable[Seq[PageButtonJson]] and
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
      (JsPath \ "selectModel").readNullable[String] and
      (JsPath \ "selectSourceField").readNullable[String] and
      (JsPath \ "selectTargetID").readNullable[String] and
      (JsPath \ "selectFilter").readNullable[String] and
      (JsPath \ "links").readNullable[Seq[PageFieldLinkJson]]
    ).apply(PageFieldJson.apply _)

  implicit def linkReads: Reads[PageFieldLinkJson] = (
    (JsPath \ "page").read[String] and
      (JsPath \ "filter").read[String]
    ).apply(PageFieldLinkJson.apply _)

  implicit def buttonReads: Reads[PageButtonJson] = (
    (JsPath \ "label").read[String] and
      (JsPath \ "fxn").read[String]
    ).apply(PageButtonJson.apply _)

}
