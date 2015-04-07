package artifacts

import com.tantalim.nodes.{SmartNodeSet, SmartNodeInstance, TntInt}
import services.TantalimPreSave

case class GeneralCounter(modelName: String, fieldName: String) extends TantalimPreSave {
  override def preSave(row: SmartNodeInstance): Unit = {
    GeneralCounter.order(row.getChild(modelName), fieldName)
  }
}

class TablePreSave extends TantalimPreSave {
  override def preSave(row: SmartNodeInstance): Unit = {
    GeneralCounter.order(row.getChild("columns"), "DisplayOrder")
    GeneralCounter.order(row.getChild("indexes"), "priority")
    row.getChild("indexes").foreach { index =>
      GeneralCounter.order(index.getChild("columns"), "IndexColumnOrder")
    }
  }
}

class MenuContentPreSave extends GeneralCounter("content", "MenuContentDisplayOrder")

class MenuItemPreSave extends GeneralCounter("items", "MenuItemDisplayOrder")

object GeneralCounter {
  def order(childSet: SmartNodeSet, fieldName: String) = {
    var counter = 10
    childSet.foreach { row =>
      row.set(fieldName, TntInt(counter))
      counter += 10
    }
  }
}