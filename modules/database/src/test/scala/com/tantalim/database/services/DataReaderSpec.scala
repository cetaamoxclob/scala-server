package com.tantalim.database.services

import java.sql._

import com.tantalim.database.data.DatabaseConnection
import com.tantalim.models._
import org.junit.runner.RunWith
import org.specs2.mock._
import org.specs2.mutable._
import org.specs2.runner._

@RunWith(classOf[JUnitRunner])
class DataReaderSpec extends Specification with Mockito with FakeArtifacts {
  "DataReader" should {
    val model = {
      val module = fakeModule()
      val personID = fakeModelFieldMap("PersonID", "person_id", DataType.Integer)
      val model = new Model(
        "PersonTest",
        basisTable = new DeepTable("Person", "person", module),
        limit = 100,
        instanceID = Option(personID._2),
        fields = Map(
          personID,
          fakeModelFieldMap("PersonName", "name")
        )
      )
      val personPhoneID = fakeModelFieldMap("PersonPhoneID", "phone_id", DataType.Integer)
      model.addChild(new Model(
        "PersonPhone",
        basisTable = new DeepTable("Phone", "phone", module),
        instanceID = Option(personPhoneID._2),
        parentField = Some("PersonID"),
        childField = Some("PersonPhonePersonID"),
        fields = Map(
          personPhoneID,
          fakeModelFieldMap("PersonPhonePersonID", "person_id", DataType.Integer),
          fakeModelFieldMap("PersonPhoneNumber", "phone_number")
        )
      ))
      model
    }

    "no results" in {
      val mockConnection = mock[Connection]
      val mockResultSet = mock[ResultSet]
      mockResultSet.next() returns false

      val reader = new DataReader with DatabaseConnection {
        override def getConnection: Connection = mockConnection

        override def query(sql: String, numberedParameters: List[Any]): ResultSet = mockResultSet
      }

      "queryModelData" in {
        reader.queryModelData(model).rows must have length 0
      }
      "calcTotalRows" in {
        reader.calcTotalRows(model) must be equalTo 0
      }
      "with filter" in {
        reader.queryModelData(model, filter = Some("PersonID GreaterThan 1")).rows must have length 0
      }
    }

    "parent has results" in {
      val mockConnection = mock[Connection]
      val mockResultSet = mock[ResultSet]
      mockResultSet.next() returns(true, true, false)

      val reader = new DataReader with DatabaseConnection {
        override def getConnection: Connection = mockConnection

        override def query(sql: String, numberedParameters: List[Any]): ResultSet = mockResultSet
      }
      reader.queryModelData(model).rows must have length 2
    }

  }
}
