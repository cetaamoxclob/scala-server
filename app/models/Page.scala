package models

import play.api.libs.functional.syntax._
import play.api.libs.json.Writes._
import play.api.libs.json._

case class Page(
                 name: String,
                 title: String,
                 icon: Option[String],
                 css: Option[String],
                 model: Model,
                 fields: Seq[PageField],
                 hasFormView: Boolean,
                 hasTableView: Boolean,
                 hasNavigation: Boolean,
                 children: Seq[Page]
                 ) {
  def fieldLengthInTableView: Int = {
    fields.filter {
      field =>
        field.showInTableView
    }.length
  }

  def toClientJson = JsObject(Seq(
    "name" -> JsString(name),
    "model" -> model.toClientJson
  ))

}

case class PageField(
                      name: String,
                      label: String,
                      showInFormView: Boolean,
                      showInTableView: Boolean,
                      showInNavigation: Boolean,
                      filter: Option[String]
                      )

object Page {
}
