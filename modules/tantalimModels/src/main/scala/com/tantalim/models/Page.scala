package com.tantalim.models

case class ShallowPage(name: String,
                       title: String,
                       icon: Option[String])

case class Page(name: String,
                title: String,
                icon: Option[String],
                css: Option[String],
                sections: Seq[PageSection]
                 )

case class PageSection(name: String,
                       title: String,
                       model: Model,
                       fields: Seq[PageField],
                       hasFormView: Boolean,
                       hasTableView: Boolean,
                       hasNavigation: Boolean,
                       viewMode: String,
                       parent: Option[PageSection],
                       var sections: Seq[PageSection]
                        ) {

  private lazy val rootDepth: Int = 1

  lazy val hasSibling: Boolean = {
    if (parent.isEmpty) false
    else parent.get.sections.length > 1
  }

  lazy val depth: Int = {
    parent match {
      case Some(section) => section.depth + 1
      case None => rootDepth
    }
  }

  def fieldLengthInTableView: Int = {
    fields.count {
      field =>
        field.showInTableView
    }
  }

  lazy val canSave = model.allowInsert || model.allowUpdate || model.allowDelete
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

