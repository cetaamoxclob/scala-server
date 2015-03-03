package com.tantalim.models

case class Model(name: String,
                 basisTable: Table,
                 limit: Int,
                 instanceID: Option[String],
                 fields: Map[String, ModelField],
                 children: Map[String, Model],
                 steps: Map[Int, ModelStep],
                 parentLink: Option[ModelParentLink],
                 orderBy: Seq[ModelOrderBy],
                 allowInsert: Boolean = true,
                 allowUpdate: Boolean = true,
                 allowDelete: Boolean = true
                  )

case class ModelField(name: String,
                      basisColumn: TableColumn,
                      step: Option[String] = None,
                      updateable: Boolean = true,
                      required: Boolean = false
                       )

case class ModelStep(table: Table,
                     required: Boolean,
                     steps: Map[Int, ModelStep])

case class ModelOrderBy(fieldName: String,
                        ascending: Option[Boolean])

case class ModelParentLink(parentField: String, childField: String)
