package mock

import com.tantalim.models.{ModelStep, TableColumn, ModelField}

trait FakeArtifacts {
  def fakeModelFieldMap(fieldName: String, dbName: String, dataType: String = "String", step: Option[ModelStep] = None,
                        updateable: Boolean = true, required: Boolean = false) =
    fieldName -> fakeModelField(fieldName, dbName, dataType, step, updateable, required)

  def fakeModelField(fieldName: String, dbName: String, dataType: String = "String", step: Option[ModelStep] = None,
                     updateable: Boolean = true, required: Boolean = false) = new ModelField(
    fieldName, fakeTableColumn(fieldName, dbName, dataType),
    step, updateable, required
  )

  def fakeTableColumnMap(name: String,
                         dbName: String, dataType: String = "String") =
    name -> fakeTableColumn(name, dbName, dataType)

  def fakeTableColumn(name: String,
                      dbName: String, dataType: String = "String") =
    new TableColumn(name = name, dbName = dbName, dataType = dataType, label = name)

}
