package data

import models.Model

import scala.collection.mutable.ListBuffer

case class SmartNodeSet(model: Model,
                        rows: ListBuffer[SmartNodeInstance] = ListBuffer.empty[SmartNodeInstance],
                        parentInstance: Option[SmartNodeInstance] = None
                         ) {

  def insert(newInstance: SmartNodeInstance): SmartNodeInstance = {
    rows += newInstance
    newInstance
  }

  def insert: SmartNodeInstance = {
    insert(new SmartNodeInstance(nodeSet = this, state = DataState.Inserted))
  }

  def foreach(f: (SmartNodeInstance) => Unit) = {
    rows.foreach(i => f(i))
  }


}

object SmartNodeSet {
  implicit def smartSetWriter = {

  }

  implicit def smartSetReader = {

  }

}