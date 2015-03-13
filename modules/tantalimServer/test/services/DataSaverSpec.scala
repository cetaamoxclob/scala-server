package services

import java.sql._

import com.tantalim.models._
import com.tantalim.nodes.{TntInt, TntString, DataState, SmartNodeSet}
import data._
import mock.{FakeConnection, FakeResultSet}
import org.junit.runner.RunWith
import org.specs2.mock._
import org.specs2.mutable._
import org.specs2.runner._
import play.api.libs.json.Json


@RunWith(classOf[JUnitRunner])
class DataSaverSpec extends Specification with Mockito with FakeArtifacts {
  "DataSaver" should {
    val model = new Model(
      "PersonTest",
      basisTable = new ShallowTable(
        "Person", "person", columns = Map.empty
      ),
      limit = 100,
      instanceID = Option("PersonID"),
      fields = Map(
        fakeModelFieldMap("PersonID", "person_id", DataType.Integer, updateable = false),
        fakeModelFieldMap("PersonName", "name", required = true)
      ),
      children = Map("PersonPhone" -> new Model(
        "PersonPhone",
        basisTable = new ShallowTable(
          "Phone", "phone", columns = Map.empty
        ),
        limit = 100,
        instanceID = Option("PersonPhoneID"),
        parentLink = Some(new ModelParentLink("PersonID", "PersonPhonePersonID")),
        fields = Map(
          fakeModelFieldMap("PersonPhoneID", "phone_id", DataType.Integer, updateable = false),
          fakeModelFieldMap("PersonPhonePersonID", "person_id", DataType.Integer, updateable = false),
          fakeModelFieldMap("PersonPhoneNumber", "phone_number", required = true)
        ),
        children = Map.empty,
        steps = Map.empty,
        orderBy = Seq.empty
      ))
    )

    "do nothing" in {
      val saver = new DataSaver with Database {
        override def getConnection: Connection = new FakeConnection
      }
      val saving = new SmartNodeSet(model)
      saver.saveAll(saving) must be equalTo saving
    }

    "insert one parent row" in {
      val saver = new DataSaver with Database {
        override def getConnection: Connection = new FakeConnection
        override def insert(sql: String, numberedParameters: List[Any], connection: Connection): ResultSet = {
          sql must be equalTo "INSERT INTO `person` (`name`) VALUES (?)"
          numberedParameters must be equalTo List(TntString("Foo"))
          new FakeResultSet {
            override def next(): Boolean = true
            override def getInt(columnIndex: Int): Int = 1
          }
        }
      }

      val _fakeID = 1
      val saving = new SmartNodeSet(model)
      saving.insert.set("PersonName", TntString("Foo"))

      saver.saveAll(saving)
      saving.rows.head.id.get must be equalTo TntInt(_fakeID)
      saving.rows.head.get("PersonID").get must be equalTo TntInt(_fakeID)
    }

    "update one parent row" in {
      val saver = new DataSaver with Database {
        override def getConnection: Connection = new FakeConnection
        override def update(sql: String, numberedParameters: List[Any], connection: Connection): Int = {
          sql must be equalTo "UPDATE `person` SET `name` = ? WHERE `person_id` = ?"
          numberedParameters must be equalTo List(TntString("Foo"), TntInt(12))
          1
        }
      }

      val saving = new SmartNodeSet(model)
      val sampleRow = saving.insert
      sampleRow.state = DataState.Updated
      sampleRow.setId(TntInt(12))
      sampleRow.set("PersonName", TntString("Foo"))
      saver.saveAll(saving)
      sampleRow.state must be equalTo DataState.Done
    }

    "delete one parent row" in {
      val saver = new DataSaver with Database {
        override def getConnection: Connection = new FakeConnection

        override def update(sql: String, numberedParameters: List[Any], connection: Connection): Int = {
          sql must be equalTo "DELETE FROM `person` WHERE `person_id` = ?"
          numberedParameters must be equalTo List(TntInt(12))
          1
        }

        override def query(sql: String, numberedParameters: List[Any]): ResultSet = {
          numberedParameters must be equalTo List(TntInt(12))
          new FakeResultSet {
            var counter = 0

            override def next(): Boolean = {
              counter += 1
              counter <= 1
            }

            override def getInt(columnName: String): Int = 12
          }
        }
      }

      val saving = new SmartNodeSet(model)
      val sampleRow = saving.insert
      sampleRow.state = DataState.Deleted
      sampleRow.setId(TntInt(12))
      saver.saveAll(saving)

      sampleRow.state must be equalTo DataState.Done
    }

    "update one child row" in {
      val saver = new DataSaver with Database {
        override def getConnection: Connection = new FakeConnection
        override def update(sql: String, numberedParameters: List[Any], connection: Connection): Int = {
          sql must be equalTo "UPDATE `phone` SET `phone_number` = ? WHERE `phone_id` = ?"
          numberedParameters must be equalTo List("(123) 456-7890", 34)
          1
        }
      }

      val saving = new SmartNodeSet(model)
      val sampleRow = saving.insert
      sampleRow.state = DataState.ChildUpdated
      sampleRow.setId(TntInt(12))
      // TODO add in child view for tests
      Json.obj(
        "children" -> Json.obj(
          "PersonPhone" -> Json.arr(
            Json.obj(
              "state" -> DataState.Updated.toString,
              "id" -> "12",
              "data" -> Json.obj(
                "PersonPhoneID" -> 34,
                "PersonPhonePersonID" -> 12,
                "PersonPhoneNumber" -> "(123) 456-7890"
              )
            )
          )
        )
      )
      saver.saveAll(saving)

      sampleRow.state must be equalTo DataState.Done
    }

    "insert one parent and child row" in pending {
//      val saver = new DataSaver with Database {
//        override def insert(sql: String, numberedParameters: List[Any]): ResultSet = {
//          sql must be equalTo "INSERT INTO `person` (`name`) VALUES (?)"
//          numberedParameters must be equalTo List("Foo")
//          new FakeResultSet {
//            override def next(): Boolean = {
//              return true
//            }
//
//            override def getLong(columnIndex: Int): Long = {
//              return 1
//            }
//          }
//        }
//      }
//
//      val _fakeID = "1"
//      val saving = new DataInstance(
//        data = Json.obj(
//          ("PersonName", "Foo")
//        ),
//        children = Map(
//          ("phones", Seq(
//
//          ))
//        )
//      )
//      val result = saver.insertSingleRow(model, saving)
//      result must be equalTo saving
      "" must be equalTo ""
    }


  }
}
