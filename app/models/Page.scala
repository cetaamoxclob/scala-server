package models

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
}

case class PageField(
                      name: String,
                      label: String,
                      showInFormView: Boolean,
                      showInTableView: Boolean,
                      showInNavigation: Boolean,
                      filter: Option[String]
                      )
