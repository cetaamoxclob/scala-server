package data

import models.Model

import scala.collection.mutable.ListBuffer

case class SmartNodeSet(model: Model,
                        rows: ListBuffer[SmartNodeInstance] = ListBuffer.empty[SmartNodeInstance],
                        parentInstance: Option[SmartNodeInstance] = None
                         ) {

  def insert(newInstance: SmartNodeInstance) = {
    rows += newInstance
    newInstance
  }

  def insert = {
    insert(new SmartNodeInstance(nodeSet = this, state = DataState.Inserted))
  }
}
