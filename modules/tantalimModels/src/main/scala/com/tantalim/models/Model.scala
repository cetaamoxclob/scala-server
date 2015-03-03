package com.tantalim.models

case class Model(name: String,
                 basisTable: Table,
                 limit: Int,
                 instanceID: Option[String],
                 fields: Map[String, ModelField],
                 children: Map[String, Model],
                 steps: scala.collection.Map[Int, ModelStep], // I'm not sure why just ": Map[Int," ... won't work here
                 parentLink: Option[ModelParentLink],
                 orderBy: Seq[ModelOrderBy],
                 allowInsert: Boolean = true,
                 allowUpdate: Boolean = true,
                 allowDelete: Boolean = true
                  )

case class ModelField(name: String,
                      basisColumn: TableColumn,
                      step: Option[ModelStep] = None,
                      updateable: Boolean = true,
                      required: Boolean = false
                       )

case class ModelStep(name: String,
                     tableAlias: Int,
                     join: TableJoin,
                     required: Boolean,
                     parentAlias: Int)

case class ModelOrderBy(fieldName: String,
                        ascending: Option[Boolean])

case class ModelParentLink(parentField: String, childField: String)
