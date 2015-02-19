package models

import play.api.libs.json._

case class ShallowPage(name: String,
                       title: String,
                       icon: Option[String])

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
                 viewMode: String,
                 children: Seq[Page]
                 ) {

  def fieldLengthInTableView: Int = {
    fields.count {
      field =>
        field.showInTableView
    }
  }

  def toClientJson: JsObject = JsObject(Seq(
    "name" -> JsString(name),
    "model" -> model.toClientJson,
    "viewMode" -> JsString(viewMode),
    "children" -> JsArray(children.toSeq.map(childPage => {
      childPage.toClientJson
    }))
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
                      select: Option[PageFieldSelect],
                      links: Option[Seq[PageFieldLinks]]
                      )

case class PageFieldSelect(model: String,
                           sourceValue: String,
                           targetID: String,
                           where: Option[String],
                           otherMappings: Option[String]
                            )

case class PageFieldLinks(page: ShallowPage, filter: String)

