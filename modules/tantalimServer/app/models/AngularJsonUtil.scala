package models


import com.tantalim.models.{Model, Page, User}
import play.api.libs.json._

import scala.collection.mutable.ListBuffer

object AngularJsonUtil {
  def toJson(page: Page): JsObject = JsObject(Seq(
    "name" -> JsString(page.name),
    "model" -> toJson(page.model),
    "viewMode" -> JsString(page.viewMode),
    "children" -> JsArray(page.children.toSeq.map(childPage => {
      AngularJsonUtil.toJson(childPage)
    }))
  ))

  def toJson(model: Model, parent: Option[Model] = None): JsObject = {
    val modelProperties = ListBuffer(
      "name" -> JsString(model.name),
      "children" -> JsArray(model.children.values.toSeq.map(childModel => {
        AngularJsonUtil.toJson(childModel, Some(model))
      }))
    )
    if (parent.isDefined) {
      modelProperties.append("parent" -> JsString(parent.get.name))
    }
    JsObject(modelProperties)
  }


  def toJson(user: User): JsObject = JsObject(Seq(
    "id" -> JsString(user.id),
    "username" -> JsString(user.username),
    "displayName" -> JsString(user.displayName)
  ))
}
