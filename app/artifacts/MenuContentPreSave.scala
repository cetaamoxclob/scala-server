package artifacts

import data.{SmartNodeInstance, TntInt}
import services.TantalimEval

class MenuContentPreSave extends TantalimEval {
   override def preSave(row: SmartNodeInstance): Unit = {
     var counter = 10
     row.getChild("content").foreach { column =>
       column.set("MenuContentDisplayOrder", TntInt(counter))
       counter += 10
     }
   }
 }
