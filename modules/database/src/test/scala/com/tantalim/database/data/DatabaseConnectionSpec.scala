package com.tantalim.database.data

import java.sql._

import org.junit.runner.RunWith
import org.mockito.Matchers
import org.specs2.mock.Mockito
import org.specs2.mutable.Specification
import org.specs2.runner.JUnitRunner

@RunWith(classOf[JUnitRunner])
class DatabaseConnectionSpec extends Specification with Mockito {

  val expected: ResultSet = null

  "DatabaseConnection" should {
    "insert" in {
      val mockStatement = mock[Statement]
      val db = new DatabaseConnection {
        val mockConnection = mock[Connection]
        val mockResultSet = mock[ResultSet]
        mockConnection.createStatement returns mockStatement
        mockStatement.getGeneratedKeys returns mockResultSet
        override def getConnection: Connection = mockConnection
      }
      db.insert("INSERT STATEMENT HERE", List.empty, db.getConnection)
      there was one(mockStatement).executeUpdate(Matchers.eq("INSERT STATEMENT HERE"), Matchers.anyInt())
    }
  }

}
