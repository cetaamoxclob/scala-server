package models

import play.api.libs.json.{JsArray, JsString, JsObject}

case class Model(name: String,
                 basisTable: Table,
                 limit: Int,
                 instanceID: Option[String],
                 fields: Map[String, ModelField],
                 children: Map[String, Model],
                 steps: Map[Int, ModelStep],
                 orderBy: Seq[ModelOrderBy]
                  ) {
  def toClientJson(): JsObject = {
    JsObject(Seq(
      "name" -> JsString(name),
      "children" -> JsArray(children.values.toSeq.map(childModel => {
        childModel.toClientJson(this)
      }))
    ))
  }

  private def toClientJson(parentModel: Model): JsObject = {
    JsObject(Seq(
      "name" -> JsString(name),
      "children" -> JsArray(children.values.toSeq.map(childModel => {
        childModel.toClientJson(this)
      })),
      "parent" -> JsString(parentModel.name)
    ))
  }
}

case class ModelField(name: String,
                      dbName: String,
                      dataType: String,
                      updateable: Boolean,
                      required: Boolean
                       )

case class ModelStep(table: Table,
                     required: Boolean,
                     fields: Map[String, ModelField],
                     steps: Map[Int, ModelStep])

case class ModelOrderBy(fieldName: String,
                        ascending: Option[Boolean])
