package artifacts

import com.tantalim.nodes.{SmartNodeInstance, TntInt}
import services.TantalimPreSave

case class GeneralCounter(modelName: String, fieldName: String) extends TantalimPreSave {
  override def preSave(row: SmartNodeInstance): Unit = {
    var counter = 10
    row.getChild(modelName).foreach { column =>
      column.set(fieldName, TntInt(counter))
      counter += 10
    }
  }
}

class TablePreSave extends GeneralCounter("columns", "DisplayOrder")

class MenuContentPreSave extends GeneralCounter("content", "MenuContentDisplayOrder")

class MenuItemPreSave extends GeneralCounter("items", "MenuItemDisplayOrder")
