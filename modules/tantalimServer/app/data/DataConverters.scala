package data

import com.tantalim.models.{ModelField, Model}
import play.api.libs.json._

/**
 * Convert incoming JSON request into a valid SmartNodeSet
 */
class DataSaveReader(model: Model, jsRequest: JsValue) {

  implicit def smartSetNodeReads: Reads[SmartNodeSet] = new Reads[SmartNodeSet] {
    override def reads(json: JsValue): JsResult[SmartNodeSet] = {
      println(json)
      JsSuccess(new SmartNodeSet(model))
    }
  }

  def validate(): JsResult[SmartNodeSet] = {
    jsRequest.validate[SmartNodeSet]
  }
}

object DataConverters {
  def convertJsArrayToSmartNodeSet(smartNodeSet: SmartNodeSet, arraySource: JsArray): Unit = {
    arraySource.value.foreach {
      case JsObject(jsObject) =>
        val smartInstance = smartNodeSet.insert
        convertJsObjectToSmartNodeInstance(smartInstance, JsObject(jsObject))
      case _ => throw new Exception("Expected JsObject")
    }
  }

  def convertJsObjectToSmartNodeInstance(smartInstance: SmartNodeInstance, objectSource: JsObject): Unit = {
    def readNullable(fieldName: String) = convertJsValueToTntValue(objectSource \ "id") match {
      case TntNull() => None
      case value => Some(value)
    }
    smartInstance.id = convertJsValueToTntValue(objectSource \ "id") match {
      case TntNull() => None
      case value => Some(value)
    }
    smartInstance.tempID = convertJsValueToTntValue(objectSource \ "tempID") match {
      case TntTempID(value) => Some(TntTempID(value))
      case TntString(value) => Some(TntTempID(java.util.UUID.fromString(value)))
      case _ => None
    }
    smartInstance.state = (objectSource \ "state").validate[DataState] match {
      case state: JsSuccess[DataState] => state.get
      case _ => DataState.Done
    }

    smartInstance.model.fields.foreach {
      case (fieldName: String, modelField: ModelField) =>
        val value = objectSource \ "data" \ fieldName
        value match {
          case _: JsUndefined | JsNull => // Don't do anything
          case _ => smartInstance.set(fieldName, DataConverters.convertJsValueToTntValue(value))
        }
    }

    smartInstance.model.children.foreach {
      case (childModelName: String, childModel: Model) =>
        objectSource \ "children" \ childModelName match {
          case childSource: JsArray =>
            println(childModelName + " -- " + childSource)
            val childSmartSet = new SmartNodeSet(childModel, parentInstance = Some(smartInstance))
            smartInstance.children += (childModelName -> childSmartSet)
            convertJsArrayToSmartNodeSet(childSmartSet, childSource)
          case _ =>
        }
    }
  }

  implicit def dataStateReads: Reads[DataState] = new Reads[DataState] {
    override def reads(json: JsValue): JsResult[DataState] = {
      json match {
        case JsString(s) =>
          try {
            JsSuccess(DataState.fromString(s))
          } catch {
            case _: IllegalArgumentException => JsError(s"Failed to convert DataState value from $s")
          }
        case _ => JsError("String value expected")
      }
    }
  }

  implicit def dataStateWrites: Writes[DataState] = new Writes[DataState] {
    override def writes(o: DataState): JsValue = JsString(o.toString)
  }

  def convertSmartNodeSetToJsonArr(set: SmartNodeSet): JsArray = {
    val rowSeq: Seq[JsObject] = set.rows.map(i => convertSmartNodeInstanceToJsonObj(i))
    JsArray(rowSeq)
  }

  def convertSmartNodeInstanceToJsonObj(instance: SmartNodeInstance): JsObject = {
    var result = Json.obj(
      "data" -> JsObject(instance.data.map {
        case (fieldName: String, value: TntValue) => (fieldName, convertTntValueToJsValue(value))
      }.toSeq)
    )
    if (instance.id.isDefined) {
      result +=("id", convertTntValueToJsValue(instance.id.get))
    }
    if (instance.tempID.isDefined) {
      result +=("tempID", convertTntValueToJsValue(instance.tempID.get))
    }
    if (!instance.children.isEmpty) {
      val childInstances = instance.children.map {
        case (childName: String, childSet: SmartNodeSet) => (childName, convertSmartNodeSetToJsonArr(childSet))
      }.toSeq
      result +=("children", JsObject(childInstances))
    }

    result
  }

  def convertTntValueToJsValue(tntValue: TntValue): JsValue = {
    tntValue match {
      case TntString(value) => JsString(value)
      case TntDecimal(value) => JsNumber(value)
      case TntInt(value) => JsNumber(value.toLong)
      case TntBoolean(value) => JsBoolean(value)
      case TntTempID(value) => JsString(value.toString)
      case TntDate(value) => JsString(value.toString)
      // Format the dates correctly
      // http://stackoverflow.com/questions/10286204/the-right-json-date-format
      // http://stackoverflow.com/questions/3914404/how-to-get-current-moment-in-iso-8601-format
      case TntNull() => JsNull
    }
  }

  def convertJsValueToTntValue(jsValue: JsValue): TntValue = {
    jsValue match {
      case JsString(value) => TntString(value)
      case JsNumber(value) => TntDecimal(value)
      case JsBoolean(value) => TntBoolean(value)
      case JsNull => new TntNull
      case _: JsUndefined => new TntNull
      case _ => throw new Exception(s"Parameters of type ${jsValue.getClass} is not supported for value $jsValue")
    }
  }


}
