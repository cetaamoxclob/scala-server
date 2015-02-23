package models

import play.api.libs.json.{JsArray, JsString, JsObject}
import play.api.mvc.QueryStringBindable

case class Model(name: String,
                 basisTable: Table,
                 limit: Int,
                 instanceID: Option[String],
                 fields: Map[String, ModelField],
                 children: Map[String, Model],
                 steps: Map[Int, ModelStep],
                 parentLink: Option[ModelParentLink],
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
                      basisColumn: TableColumn,
                      step: Option[String] = None,
                      updateable: Boolean = true,
                      required: Boolean = false
                       )

case class ModelStep(table: Table,
                     required: Boolean,
                     fields: Map[String, ModelField],
                     steps: Map[Int, ModelStep])

case class ModelOrderBy(fieldName: String,
                        ascending: Option[Boolean])

case class ModelParentLink(parentField: String, childField: String)

object ModelOrderBy {
  implicit def orderByBindable(implicit stringBinder: QueryStringBindable[String]) = new QueryStringBindable[ModelOrderBy] {
    override def bind(key: String, params: Map[String, Seq[String]]): Option[Either[String, ModelOrderBy]] = {
      println(params)
      for {
        fieldName <- stringBinder.bind(key, params)
      } yield {
        fieldName match {
          case Right(fieldName) => Right(ModelOrderBy(fieldName, None))
          case _ => Left("")
        }
      }
    }

    override def unbind(key: String, pager: ModelOrderBy): String = {
      ""
    }
  }
}