package controllers

case class Menu(appTitle: String, content: List[MenuContent]) {
}

case class MenuContent(title: String, items: List[MenuItem]) {

}

case class MenuItem(title: String, href: Option[String], page: Option[String]) {

  def toHref(): String = {
    if (page.isDefined)
      "/page/Hello/"
    else
      href.toString
  }
}