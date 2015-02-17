package models

case class Page(name: String,
                title: String,
                favicon: Option[String],
                css: Option[String]) {
}

object Page {
  def compile(name: String, json: PageJson): Page = {
    val page = new Page(name, json.title, json.favicon, json.css)
    page
  }
}
