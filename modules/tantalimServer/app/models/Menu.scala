package models

case class Menu(appTitle: String, content: Seq[MenuContent])

case class MenuContent(title: String, items: Seq[MenuItem])

case class MenuItem(title: String, href: String, icon: Option[Icon]) {
  def hasFontAwesome = icon.isDefined && icon.get.isFontAwesome

  def hasGlyphicon = icon.isDefined && icon.get.isGlyphicon
}
