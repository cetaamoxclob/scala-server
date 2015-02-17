package models

import play.api.libs.json.{JsString, JsObject}

case class Model(name: String) {
  def toClientJson = JsObject(Seq(
    "name" -> JsString(name)
  ))
}
