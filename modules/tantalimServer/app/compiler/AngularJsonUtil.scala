package compiler

import com.tantalim.models._
import play.api.libs.json._

import scala.collection.mutable.ListBuffer

object AngularJsonUtil {
  def toJson(page: Page): JsObject = JsObject(Seq(
    "name" -> JsString(page.name),
    "sections" -> JsArray(
      page.sections.toSeq.map(childPage => toJson(childPage))
    )
  ))

  def toJson(section: PageSection): JsObject = JsObject(Seq(
    "name" -> JsString(section.name),
    "model" -> toJson(section.model),
    "viewMode" -> JsString(section.viewMode),
    "fields" -> JsArray(
      section.fields.zipWithIndex.map {
        case (field, order) => toJson(field, order)
      }
    ),
    "sections" -> JsArray(
      section.sections.toSeq.map(childPage => toJson(childPage))
    )
  ))

  def toJson(model: Model, parent: Option[Model] = None): JsObject = {
    val modelProperties = ListBuffer(
      "name" -> JsString(model.name),
      "fields" -> JsObject(
        model.fields.map {
          case (fieldName, field) => fieldName -> toJson(field)
        }.toSeq
      ),
      "children" -> JsArray(model.children.values.toSeq.map(childModel => {
        toJson(childModel, Some(model))
      }))
    )
    if (parent.isDefined) {
      modelProperties.append("parent" -> JsString(parent.get.name))
    }
    JsObject(modelProperties)
  }

  private def toJson(field: PageField, order: Int): JsObject = JsObject(Seq(
    "name" -> JsString(field.name),
    "required" -> JsBoolean(field.required),
    "order" -> JsNumber(order),
    "fieldType" -> JsString(field.fieldType)
  ))

  private def toJson(field: ModelField): JsObject = JsObject(Seq(
    "name" -> JsString(field.name),
    "dataType" -> JsString(field.basisColumn.dataType.toString),
    "updateable" -> JsBoolean(field.updateable),
    "fieldDefault" -> {
      if (field.fieldDefault.isEmpty) JsNull
      else toJson(field.fieldDefault.get)
    }
  ))

  private def toJson(fieldDefault: FieldDefault): JsObject = JsObject(Seq(
    "type" -> JsString(fieldDefault.defaultType.toString),
    "overwrite" -> JsBoolean(fieldDefault.overwrite),
    "value" -> JsString(fieldDefault.value)
  ))

  def toJson(user: User): JsObject = JsObject(Seq(
    "id" -> JsString(user.id),
    "username" -> JsString(user.username),
    "displayName" -> JsString(user.displayName)
  ))
}
