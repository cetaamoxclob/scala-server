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
      "children" -> JsArray(
        model.children.values.toSeq.map(childModel =>
          if (childModel.isRecursive) Json.obj(
            "extends" -> JsString(childModel.name)
          )
          else toJson(childModel, Some(model))
        )
      )
    )
    if (parent.isDefined) {
      modelProperties.append("parent" -> JsString(parent.get.name))
    }
    if (model.customUrlSource.isDefined) {
      modelProperties.append("customUrlSource" -> JsString(model.customUrlSource.get))
    }
    JsObject(modelProperties)
  }

  private def toJson(field: PageField, order: Int): JsObject = JsObject(Seq(
    "name" -> JsString(field.name),
    "required" -> JsBoolean(field.required),
    "order" -> JsNumber(order),
    "fieldType" -> JsString(field.fieldType.lower)
  ))

  private def toJson(field: ModelField): JsObject = JsObject(Seq(
    "name" -> JsString(field.name),
    "dataType" -> JsString(field.basisColumn.dataType.toString),
    "updateable" -> JsBoolean(field.updateable),
    "alwaysDefault" -> JsBoolean(field.alwaysDefault),
    "fieldDefault" -> toJson(field.fieldDefault),
    "functionDefault" -> toJson(field.functionDefault),
    "valueDefault" -> toJson(field.valueDefault)
  ))

  def toJson(option: Option[String]): JsValue = if (option.isEmpty) JsNull else JsString(option.get)

  def toJson(user: User): JsObject = JsObject(Seq(
    "id" -> JsString(user.id),
    "username" -> JsString(user.username),
    "displayName" -> JsString(user.displayName)
  ))
}
