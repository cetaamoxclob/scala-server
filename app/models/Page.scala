package models

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
    fields.count {
      field =>
        field.showInTableView
    }
  }

  def toClientJson = JsObject(Seq(
    "name" -> JsString(name),
    "model" -> model.toClientJson
  ))

}

case class PageField(
                      name: String,
                      fieldType: String,
                      label: String,
                      required: Boolean,
                      disabled: Boolean,
                      showInFormView: Boolean,
                      showInTableView: Boolean,
                      showInNavigation: Boolean,
                      placeholder: Option[String],
                      help: Option[String],
                      filter: Option[String],
                      blurFunction: Option[String],
                      select: Option[String],
                      links: Option[Seq[PageFieldLinks]]
                      )

case class PageFieldLinks(page: String, filter: String)

case class PageFieldSelect(model: String,
                           sourceValue: String,
                           targetID: String,
                           where: String,
                           otherMappings: Option[Seq[String]]
                            )
