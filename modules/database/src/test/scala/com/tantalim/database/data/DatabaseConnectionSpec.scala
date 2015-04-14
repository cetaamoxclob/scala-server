package com.tantalim.database.data

import java.sql._

import com.tantalim.database.mock.FakeConnection
import org.junit.runner.RunWith
import org.specs2.mutable.Specification
import org.specs2.runner.JUnitRunner

@RunWith(classOf[JUnitRunner])
class DatabaseConnectionSpec extends Specification {

  val expected: ResultSet = null

  "ComparatorByDataType" should {
    "Boolean" in {
      val db = new DatabaseConnection {
        override def getConnection: Connection = new FakeConnection
      }
      val rs = db.insert("INSERT", List.empty, db.getConnection)
      rs must be equalTo(expected)
    }
  }

}
