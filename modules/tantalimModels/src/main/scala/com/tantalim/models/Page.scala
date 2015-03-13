package com.tantalim.models

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
                 parentPage: Option[Page],
                 children: Seq[Page]
                 ) {

  def fieldLengthInTableView: Int = {
    fields.count {
      field =>
        field.showInTableView
    }
  }

  private val rootDepth: Int = 1

  def depth: Int = {
    parentPage match {
      case Some(page) => page.depth + 1
      case None => rootDepth
    }
  }

}

case class PageField(
                      name: String,
                      fieldType: String,
                      modelField: ModelField,
                      label: String,
                      required: Boolean,
                      disabled: Boolean,
                      searchable: Boolean,
                      showInFormView: Boolean,
                      showInTableView: Boolean,
                      showInNavigation: Boolean,
                      placeholder: Option[String],
                      help: Option[String],
                      filter: Option[String],
                      blurFunction: Option[String],
                      select: Option[PageFieldSelect],
                      links: Seq[PageFieldLink]
                      ) {

  def tableWidth = {
    dataType match {
      case DataType.Boolean => 5
      case DataType.String => 20
      case _ => 10
    }
  }

  private def dataType = modelField.basisColumn.dataType

  def alignRight = isNumericType

  def alignCenter = isBoolean

  def isBoolean = {
    dataType == DataType.Boolean
  }

  def isNumericType = {
    dataType == DataType.Integer || dataType == DataType.Decimal
  }

  def isDateType = {
    dataType == DataType.Date || dataType == DataType.DateTime
  }
}

case class PageFieldSelect(model: String,
                           sourceField: String,
                           targetID: Option[String],
                           fields: Map[String, String],
                           filter: Option[String]
                            )

case class PageFieldLink(page: ShallowPage, filter: String)

