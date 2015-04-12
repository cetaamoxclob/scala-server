package artifacts

import com.tantalim.database.services.TantalimPreSave
import com.tantalim.nodes.{SmartNodeSet, SmartNodeInstance, TntInt}

case class GeneralCounter(modelName: String, fieldName: String) extends TantalimPreSave {
  override def preSave(row: SmartNodeInstance): Unit = {
    GeneralCounter.order(row.getChild(modelName), fieldName)
  }
}

class TablePreSave extends TantalimPreSave {
  override def preSave(row: SmartNodeInstance): Unit = {
    GeneralCounter.order(row.getChild("columns"), "DisplayOrder")
    GeneralCounter.order(row.getChild("indexes"), "priority")
    if (row.getChild("indexes").isDefined) {
      row.getChild("indexes").get.foreach { index =>
        GeneralCounter.order(index.getChild("columns"), "IndexColumnOrder")
      }
    }
  }
}

class MenuContentPreSave extends GeneralCounter("content", "MenuContentDisplayOrder")

class MenuItemPreSave extends GeneralCounter("items", "MenuItemDisplayOrder")

object GeneralCounter {
  def order(childSet: Option[SmartNodeSet], fieldName: String) = {
    var counter = 10
    if (childSet.isDefined) {
      childSet.get.foreach { row =>
        row.set(fieldName, TntInt(counter))
        counter += 10
      }
    }
  }
}