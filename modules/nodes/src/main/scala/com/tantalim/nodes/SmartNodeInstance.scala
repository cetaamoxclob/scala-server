package com.tantalim.nodes

import com.tantalim.models.Model
import com.tantalim.util.TantalimException

import scala.collection.mutable

case class SmartNodeInstance(
                              /**
                               * reference to the parent SmartNodeSet that contains this instance
                               */
                              nodeSet: SmartNodeSet,

                              /**
                               * NO_CHANGE, DELETED, INSERTED, UPDATED
                               */
                              var state: DataState = DataState.Done,

                              /**
                               * Unique identifier for this instance
                               */
                              var id: Option[TntValue] = None,

                              /**
                               * Unique identifier for this instance
                               */
                              var tempID: Option[TntTempID] = None,

                              /**
                               * The map of columns and values for this instance
                               */
                              data: mutable.HashMap[String, TntValue] = mutable.HashMap.empty,

                              /**
                               * map of SmartNodeInstances representing the children of this node
                               */
                              children: mutable.HashMap[String, SmartNodeSet] = mutable.HashMap.empty) {
  def isRoot = nodeSet.parentInstance.isEmpty

  def delete() = {
    state = DataState.Deleted
  }

  def update() = if (state == DataState.Done) {
    state = DataState.Updated
    if (nodeSet.parentInstance.isDefined) nodeSet.parentInstance.get
  }

  def childUpdated() = if (state == DataState.Done) {
    state = DataState.ChildUpdated
    if (nodeSet.parentInstance.isDefined) nodeSet.parentInstance.get
  }

  def get(fieldName: String): Option[TntValue] = {
    if (nodeSet.model.hasField(fieldName)) data.get(fieldName)
    else if (nodeSet.parentInstance.isDefined) nodeSet.parentInstance.get.get(fieldName)
    else throw new TantalimException("Failed to find value for field named " + fieldName, "")
  }

  def set(fieldName: String, value: TntValue) = {
    if (nodeSet.model.fields.get(fieldName).isEmpty) {
      throw new TantalimException(s"Can't find field named $fieldName in ${nodeSet.model.name}",
        "Choose an existing field: " + nodeSet.model.fields.keys.mkString(", "))
    }
    data += (fieldName -> value)
  }

  def model = nodeSet.model

  def setId(value: TntValue) = {
    nodeSet.model.instanceID match {
      case Some(instanceID) =>
        id = Some(value)
        set(instanceID.name, value)
      case None => throw new Exception("InstanceID isn't defined for " + this)
    }
  }

  def getChild(childName: String): Option[SmartNodeSet] = children.get(childName)

  def foreachChild(f: (SmartNodeSet) => Unit) = {
    nodeSet.model.children.foreach {
      case (childModelName: String, childModel: Model) =>
        children.get(childModelName) match {
          case Some(childSet) => f(childSet)
          case None =>
        }
    }
  }

  def index: Int = {
    val (_, i) = nodeSet.rows.zipWithIndex.find {
      case (row, index) =>
        this == row
    }.get
    i
  }

  override def toString = {
    val idString = if (id.isDefined) id.get.toString else data.toString()
    s"${model.name}($idString)"
  }
}
