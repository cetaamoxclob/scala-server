package artifacts

import com.tantalim.database.services.TantalimPreSave
import com.tantalim.nodes.{SmartNodeSet, SmartNodeInstance, TntInt}

case class GeneralCounter(modelName: String, fieldName: String) extends TantalimPreSave {
  override def preSave(row: SmartNodeInstance): Unit = {
    GeneralCounter.order(row.getChild(modelName), fieldName)
  }
}

class TablePreSave extends TantalimPreSave {
  override def preSave(table: SmartNodeInstance): Unit = {
    GeneralCounter.order(table.getChild("columns"), "DisplayOrder")
    GeneralCounter.order(table.getChild("indexes"), "priority")
    table.foreach("indexes") { index =>
      GeneralCounter.order(index.getChild("columns"), "IndexColumnOrder")
    }
  }
}

class MenuContentPreSave extends GeneralCounter("content", "MenuContentDisplayOrder")

class MenuItemPreSave extends GeneralCounter("items", "MenuItemDisplayOrder")

class ModelPreSave extends TantalimPreSave {
  override def preSave(model: SmartNodeInstance): Unit = {
    processModel(model)
  }

  private def processModel(model: SmartNodeInstance): Unit = {
    GeneralCounter.order(model.getChild("orderBy"), "ModelSortSortOrder")
    processModels(model.getChild("children"))
  }

  private def processModels(models: Option[SmartNodeSet]): Unit = {
    if (models.isDefined) models.get.foreach(model => processModel(model))
  }
}

class PagePreSave extends TantalimPreSave {
  private def processSections(sections: Option[SmartNodeSet]): Unit = {
    if (sections.isDefined) {
      sections.get.foreach { section =>
        GeneralCounter.order(section.getChild("fields"), "PageFieldDisplayOrder")
        processSections(section.getChild("sections"))
      }
    }
  }

  override def preSave(page: SmartNodeInstance): Unit = {
    processSections(page.getChild("sections"))
  }
}

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