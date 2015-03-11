package artifacts

import data.{SmartNodeInstance, TntInt}
import services.TantalimEval

class MenuItemPreSave extends TantalimEval {
   override def preSave(row: SmartNodeInstance): Unit = {
     var counter = 10
     row.getChild("items").foreach { column =>
       column.set("MenuItemDisplayOrder", TntInt(counter))
       counter += 10
     }
   }
 }
