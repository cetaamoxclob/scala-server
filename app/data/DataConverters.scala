package data

import play.api.libs.functional.syntax._
import play.api.libs.json._

object DataConverters {
  //  def instanceWrites: Writes[SmartNodeInstance] = (
  //    (JsPath \ "state").write[DataState] and
  //      (JsPath \ "data").writeNullable[Map[String, JsValue]] and
  //      (JsPath \ "id").writeNullable[String] and
  //      (JsPath \ "tempID").writeNullable[String] and
  //      (JsPath \ "children").lazyWriteNullable(Writes.map[Seq[SmartNodeInstance]])
  //    )(unlift(SmartNodeInstance.unapply))
  //
  //  implicit def instanceReads: Reads[SmartNodeInstance] = (
  //    (JsPath \ "state").read[DataState] and
  //      (JsPath \ "data").readNullable[Map[String, JsValue]] and
  //      (JsPath \ "id").readNullable[String] and
  //      (JsPath \ "tempID").readNullable[String] and
  //      (JsPath \ "children").lazyReadNullable(Reads.map(Reads.seq[SmartNodeInstance](instanceReads)))
  //    ).apply(SmartNodeInstance.apply _)

  implicit def dataStateReads: Reads[DataState] = new Reads[DataState] {
    override def reads(json: JsValue): JsResult[DataState] = {
      json match {
        case JsString(s) => {
          try {
            JsSuccess(DataState.fromString(s))
          } catch {
            case _: IllegalArgumentException => JsError(s"Failed to convert DataState value from $s")
          }
        }
        case _ => JsError("String value expected")
      }
    }
  }

  implicit def dataStateWrites: Writes[DataState] = new Writes[DataState] {
    override def writes(o: DataState): JsValue = JsString(o.toString)
  }

  def convertSmartNodeSetToJsonArr(set: SmartNodeSet): JsArray = {
//    println(s"converting ${set.rows.length} row(s) on ${set.model.name} for ${set.parentInstance.getOrElse("ROOT")}")
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
    if (!instance.children.isEmpty) {
      val childInstances = instance.children.map{
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
