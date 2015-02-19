package services

import models.Model
import play.api.libs.json.{JsArray, JsValue}



class DataSaver {
  def saveAll(model: Model, jsArray: Option[JsValue]): JsArray = {
    if (model.instanceID.isEmpty) throw new Exception("Cannot insert/update/delete an instance without an instanceID for " + model.name)

    JsArray()
  }
}
