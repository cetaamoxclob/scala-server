package services

import play.api.db.DB
import play.api.Play.current
import play.api.libs.json.JsValue
import play.api.libs.json.Json._

class DataReader {

  def read(name: String): JsValue = {

    var outString = "Number is "
    val conn = DB.getConnection()
    try {
      val stmt = conn.createStatement
      val rs = stmt.executeQuery("SELECT '9' as testkey ")
      while (rs.next()) {
        outString += rs.getString("testkey")
      }
    } finally {
      conn.close()
    }
    println(outString)
    val response = toJson(Map("status" -> "OK", "message" -> ("Hello World " + name)))
    response
  }

}
