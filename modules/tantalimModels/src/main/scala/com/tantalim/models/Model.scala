package com.tantalim.models

case class Model(name: String,
                 basisTable: Table,
                 limit: Int = 0,
                 instanceID: Option[String] = None,
                 fields: Map[String, ModelField],
                 steps: scala.collection.Map[Int, ModelStep] = Map.empty, // I'm not sure why just ": Map[Int," ... won't work here
                 parent: Option[Model] = None,
                 parentLink: Option[ModelParentLink] = None,
                 orderBy: Seq[ModelOrderBy] = Seq.empty,
                 allowInsert: Boolean = true,
                 allowUpdate: Boolean = true,
                 allowDelete: Boolean = true,
                 preSave: Option[String] = None,
                 filter: Option[String] = None,
                 children: collection.mutable.Map[String, Model] = scala.collection.mutable.Map.empty
                  )

case class ModelField(name: String,
                      basisColumn: TableColumn,
                      step: Option[ModelStep] = None,
                      updateable: Boolean = true,
                      required: Boolean = false,
                      fieldDefault: Option[FieldDefault] = None,
                      export: Boolean = true
                       ) {
  def dataType: DataType = basisColumn.dataType
}

case class FieldDefault(value: String,
                        overwrite: Boolean,
                        defaultType: FieldDefaultType,
                        watch: Seq[String])

case class ModelStep(name: String,
                     tableAlias: Int,
                     join: TableJoin,
                     required: Boolean,
                     parentAlias: Int)

case class ModelOrderBy(fieldName: String,
                        ascending: Option[Boolean])

case class ModelParentLink(parentField: String, childField: String)
