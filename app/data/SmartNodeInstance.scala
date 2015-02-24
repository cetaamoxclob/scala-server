package data

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
                              children: Map[String, SmartNodeSet] = Map.empty) {
  def delete = {
    state = DataState.Deleted
  }

  def update = if (state == DataState.Done) {
    state = DataState.Updated
    if (nodeSet.parentInstance.isDefined) nodeSet.parentInstance.get
  }

  def childUpdated = if (state == DataState.Done) {
    state = DataState.ChildUpdated
    if (nodeSet.parentInstance.isDefined) nodeSet.parentInstance.get
  }

  def get(fieldName: String) = data.get(fieldName)

  def setupChildSets = {
    nodeSet.model.children.foreach { childModel =>
      val newSet = new SmartNodeSet(
        model = childModel._2,
        parentInstance = Some(this)
      )
      children + childModel._1 -> newSet
    }
  }

}
