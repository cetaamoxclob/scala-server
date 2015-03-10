package artifacts

import data.{SmartNodeInstance, TntInt}
import services.TantalimEval

class TablePreSave extends TantalimEval {
  override def preSave(row: SmartNodeInstance): Unit = {
    var counter = 10
    row.getChild("columns").foreach { column =>
      column.set("displayOrder", TntInt(counter))
      counter += 10
    }
  }
}
