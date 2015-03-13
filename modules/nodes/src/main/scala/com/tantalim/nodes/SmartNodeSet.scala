package com.tantalim.nodes

import com.tantalim.models.Model

import scala.collection.mutable.ListBuffer

case class SmartNodeSet(model: Model,
                        rows: ListBuffer[SmartNodeInstance] = ListBuffer.empty[SmartNodeInstance],
                        parentInstance: Option[SmartNodeInstance] = None
                         ) {

  def insert = {
    val newInstance = new SmartNodeInstance(nodeSet = this, state = DataState.Inserted)
    rows += newInstance
    newInstance
  }

  def foreach(f: (SmartNodeInstance) => Unit) = {
    rows.foreach(i => f(i))
  }

  def isEmpty = rows.isEmpty

  def deleteAll() = rows.foreach(instance => instance.state = DataState.Deleted)

}
