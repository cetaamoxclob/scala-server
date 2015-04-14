package com.tantalim.models

import com.tantalim.util.TantalimException

case class Model(name: String,
                 basisTable: DeepTable,
                 limit: Int = 0,
                 instanceID: Option[ModelField] = None,
                 fields: Map[String, ModelField],
                 steps: scala.collection.Map[Int, ModelStep] = Map.empty, // I'm not sure why just ": Map[Int," ... won't work here
                 parent: Option[Model] = None,
                 parentField: Option[String] = None,
                 childField: Option[String] = None,
                 orderBy: Seq[ModelOrderBy] = Seq.empty,
                 allowInsert: Boolean = true,
                 allowUpdate: Boolean = true,
                 allowDelete: Boolean = true,
                 preSave: Option[String] = None,
                 filter: Option[String] = None,
                 customUrlSource: Option[String] = None,
                 children: collection.mutable.Map[String, Model] = scala.collection.mutable.Map.empty
                  ) {

  def addChild(child: Model): Unit = {
    this.children(child.name) = child.copy(parent = Some(this))
  }

  /**
   * List of models ordered from the first child that should be inserted to the last child to be inserted.
   * Insert order is determined by what depends on what.
   */
  lazy val orderedChildren = {
    val dependencies = children.map { case (childModelName, childModel) =>
      childModelName -> children.values.filter { sibling =>
        sibling != childModel && childModel.steps.values.exists(step => sibling.basisTable.name == step.join.table.name)
      }
    }

    val orderedChildren = children.values.toList.sortBy { model =>
      val myDependencies = dependencies.get(model.name).get
      myDependencies.size + 10
    }

    // TODO order them one more time based on dependencies
    // A depends on B, B depends on C, then C goes first but A and B have one dependencies and may not get sorted correctly

    println("Ordered Children: " + orderedChildren.map(model => model.name).mkString(", "))
    dependencies.foreach { case (modelName, models) =>
      println(s"$modelName depends on: ${models.map(m => m.name).mkString(", ")}")
    }
    orderedChildren
  }

  lazy val isRecursive: Boolean = {
    isSameClassAsAncestors(this)
  }

  def isSameClassAsAncestors(childModel: Model): Boolean = {
    if (parent.isEmpty) false
    else if (parent.get.name == childModel.name) true
    else parent.get.isSameClassAsAncestors(childModel)
  }

  override def toString: String = s"Model($name) fields(${fields.size}) children(${children.size})"

  def hasField(fieldName: String): Boolean = fields.get(fieldName).isDefined

  def getField(fieldName: String): ModelField = {
    val field = fields.get(fieldName)
    if (field.isEmpty) {
      throw new TantalimException(s"Failed to get field named `$fieldName` from model `$name`",
        s"Try using one of the following: ${fields.keys.mkString(", ")}")
    }
    field.get
  }
}

case class ModelField(name: String,
                      basisColumn: TableColumn,
                      step: Option[ModelStep] = None,
                      updateable: Boolean = true,
                      required: Boolean = false,
                      alwaysDefault: Boolean = false,
                      fieldDefault: Option[String] = None,
                      functionDefault: Option[String] = None,
                      valueDefault: Option[String] = None,
                      export: Boolean = true
                       ) {
  def dataType: DataType = basisColumn.dataType
}

case class ModelStep(name: String,
                     tableAlias: Int,
                     join: TableJoin,
                     required: Boolean,
                     allowInsert: Boolean = false,
                     allowUpdate: Boolean = false,
                     allowDelete: Boolean = false,
                     parentAlias: Int) {
  // TODO Consider adding custom filters like AppTranslations
}

case class ModelOrderBy(fieldName: String,
                        ascending: Option[Boolean] = None)
