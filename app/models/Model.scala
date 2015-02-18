package models

import play.api.libs.json.{JsString, JsObject}

case class Model(
                  name: String,
                  basisTable: Table,
                  instanceID: Option[String],
                  fields: Map[String, ModelField]
                  ) {
  def toClientJson = JsObject(Seq(
    "name" -> JsString(name)
  ))
}

case class ModelField(
                       name: String,
                       dbName: String,
                       dataType: String,
                       updateable: Boolean,
                       required: Boolean
                       )
