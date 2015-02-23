package services

import java.sql._

import data.{DataState, Database}
import mock.{FakeArtifacts, FakeResultSet}
import models._
import org.junit.runner.RunWith
import org.specs2.mock._
import org.specs2.mutable._
import org.specs2.runner._
import play.api.libs.json.{JsNull, JsArray, Json}


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
      parentLink = None,
      fields = Map(
        fakeModelFieldMap("PersonID", "person_id", "Integer", updateable = false),
        fakeModelFieldMap("PersonName", "name", "String", required = true)
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
          fakeModelFieldMap("PersonPhoneID", "phone_id", "Integer", updateable = false),
          fakeModelFieldMap("PersonPhonePersonID", "person_id", "Integer", updateable = false),
          fakeModelFieldMap("PersonPhoneNumber", "phone_number", "String", required = true)
        ),
        children = Map.empty,
        steps = Map.empty,
        orderBy = Seq.empty
      )),
      steps = Map.empty,
      orderBy = Seq.empty
    )

    "do nothing" in {
      val saver = new DataSaver
      saver.saveAll(model, None) must be equalTo JsArray()
    }

    "insert one parent row" in {
      val saver = new DataSaver with Database {
        override def insert(sql: String, numberedParameters: List[Any]): ResultSet = {
          sql must be equalTo "INSERT INTO `person` (`name`) VALUES (?)"
          numberedParameters must be equalTo List("Foo")
          new FakeResultSet {
            override def next(): Boolean = {
              return true
            }

            override def getLong(columnIndex: Int): Long = {
              return 1
            }
          }
        }
      }

      val _fakeID = "1"
      val saving = Json.arr(
        Json.obj(
          "state" -> DataState.Inserted.toString,
          "tempID" -> "ASDF1234",
          "data" -> Json.obj(
            "PersonName" -> "Foo"
          )
        )
      )
      val result = saver.saveAll(model, Option(saving))
      result must be equalTo Json.arr(
        Json.obj(
          "id" -> _fakeID,
          "data" -> Json.obj(
            "PersonID" -> _fakeID,
            "PersonName" -> "Foo"
          )
        )
      )
    }

    "update one parent row" in {
      val saver = new DataSaver with Database {
        override def update(sql: String, numberedParameters: List[Any]): Int = {
          sql must be equalTo "UPDATE `person` SET `name` = ? WHERE `person_id` = ?"
          numberedParameters must be equalTo List("Foo", 12)
          1
        }
      }

      val sampleRow = Json.obj(
        "state" -> DataState.Updated.toString,
        "id" -> "12",
        "data" -> Json.obj(
          "PersonID" -> 12,
          "PersonName" -> "Foo"
        )
      )

      val saving = Json.arr(sampleRow)
      val result = saver.saveAll(model, Option(saving))
      result must be equalTo Json.arr(sampleRow - "state")
    }

    "delete one parent row" in {
      val saver = new DataSaver with Database {
        override def update(sql: String, numberedParameters: List[Any]): Int = {
          sql must be equalTo "DELETE FROM `person` WHERE `person_id` = ?"
          numberedParameters must be equalTo List(12)
          1
        }

        override def query(sql: String, numberedParameters: List[Any]): ResultSet = {
          numberedParameters must be equalTo List(12)
          new FakeResultSet {
            var counter = 0

            override def next(): Boolean = {
              counter += 1
              return counter <= 1
            }

            override def getInt(columnName: String): Int = {
              return 12
            }
          }
        }
      }

      val sampleRow = Json.obj(
        "state" -> DataState.Deleted.toString,
        "id" -> "12"
      )

      val saving = Json.arr(sampleRow)
      val result = saver.saveAll(model, Option(saving))
      result must be equalTo Json.arr(Json.obj(
        "id" -> "12"
      ))
    }

    "update one child row" in {
      val saver = new DataSaver with Database {
        override def update(sql: String, numberedParameters: List[Any]): Int = {
          sql must be equalTo "UPDATE `phone` SET `phone_number` = ? WHERE `phone_id` = ?"
          numberedParameters must be equalTo List("(123) 456-7890", 34)
          1
        }
      }

      val sampleRow = Json.obj(
        "state" -> DataState.ChildUpdated.toString,
        "id" -> "12",
        "data" -> Json.obj(
          "PersonID" -> 12,
          "PersonName" -> "Foo"
        ),
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

      val expectedRow = Json.obj(
        "id" -> "12",
        "data" -> Json.obj(
          "PersonID" -> 12,
          "PersonName" -> "Foo"
        ),
        "children" -> Json.obj(
          "PersonPhone" -> Json.arr(
            Json.obj(
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


      val result = saver.saveAll(model, Option(Json.arr(sampleRow)))
      result must be equalTo Json.arr(expectedRow)
    }

  }
}
