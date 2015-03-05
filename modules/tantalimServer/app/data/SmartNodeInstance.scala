package data

import com.tantalim.models.Model

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

  def get(fieldName: String) = data.get(fieldName)

  def set(fieldName: String, value: TntValue) = data += (fieldName -> value)

  def model = nodeSet.model

  def setId(value: TntValue) = {
    nodeSet.model.instanceID match {
      case Some(instanceID) =>
        id = Some(value)
        set(instanceID, value)
      case None => throw new Exception("InstanceID isn't defined for " + this)
    }
  }

  def getChild(childName: String) = {
    children.get(childName).get
  }

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
    val (_, i) = nodeSet.rows.zipWithIndex.find{
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
