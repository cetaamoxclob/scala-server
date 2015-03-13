package com.tantalim.models

trait FakeArtifacts {
  def fakeModelFieldMap(fieldName: String, dbName: String, dataType: DataType = DataType.String, step: Option[ModelStep] = None,
                        updateable: Boolean = true, required: Boolean = false) =
    fieldName -> fakeModelField(fieldName, dbName, dataType, step, updateable, required)

  def fakeModelField(fieldName: String, dbName: String, dataType: DataType, step: Option[ModelStep] = None,
                     updateable: Boolean = true, required: Boolean = false) = new ModelField(
    fieldName, fakeTableColumn(fieldName, dbName, dataType),
    step, updateable, required
  )

  def fakeTableColumnMap(name: String,
                         dbName: String, dataType: DataType) =
    name -> fakeTableColumn(name, dbName, dataType)

  def fakeTableColumn(name: String,
                      dbName: String, dataType: DataType) =
    new TableColumn(name = name, dbName = dbName, order = 0, dataType = dataType, label = name)

}
