package com.tantalim.database.services

import java.sql._

import com.tantalim.database.data.DatabaseConnection
import com.tantalim.models._
import com.tantalim.nodes.{SmartNodeSet, TntString}
import org.junit.runner.RunWith
import org.mockito.Matchers
import org.specs2.mock._
import org.specs2.mutable._
import org.specs2.runner._

@RunWith(classOf[JUnitRunner])
class DataReaderSpec extends Specification with Mockito with FakeArtifacts {
  "DataReader" should {
    val personID = fakeModelFieldMap("PersonID", "person_id", DataType.Integer, updateable = false)
    val model = new Model(
      "PersonTest",
      basisTable = new DeepTable("Person", "person", fakeModule()),
      limit = 100,
      instanceID = Option(personID._2),
      fields = Map(
        personID,
        fakeModelFieldMap("PersonName", "name", required = true)
      )
    )
    val personPhoneID = fakeModelFieldMap("PersonPhoneID", "phone_id", DataType.Integer, updateable = false)
    model.addChild(new Model(
      "PersonPhone",
      basisTable = new DeepTable(
        "Phone", "phone", fakeModule()
      ),
      limit = 100,
      instanceID = Option(personPhoneID._2),
      parentField = Some("PersonID"),
      childField = Some("PersonPhonePersonID"),
      fields = Map(
        personPhoneID,
        fakeModelFieldMap("PersonPhonePersonID", "person_id", DataType.Integer, updateable = false),
        fakeModelFieldMap("PersonPhoneNumber", "phone_number", required = true)
      ),
      steps = Map.empty,
      orderBy = Seq.empty
    ))

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
