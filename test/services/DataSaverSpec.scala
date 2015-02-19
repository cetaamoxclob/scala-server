package services

import java.sql._

import data.{DataState, Database}
import mock.FakeResultSet
import models._
import org.junit.runner.RunWith
import org.specs2.mock._
import org.specs2.mutable._
import org.specs2.runner._
import play.api.libs.json.{JsArray, Json}


@RunWith(classOf[JUnitRunner])
class DataSaverSpec extends Specification with Mockito {
  "DataSaver" should {
    val model = new Model(
      "PersonTest",
      basisTable = new ShallowTable(
        "Person", "person", columns = Map.empty
      ),
      limit = 100,
      instanceID = Option("PersonID"),
      fields = Map(
        "PersonID" -> new ModelField(
          "PersonID",
          "person_id",
          dataType = "Integer",
          updateable = true,
          required = false
        ),
        "PersonName" -> new ModelField(
          "PersonName",
          "name",
          dataType = "String",
          updateable = true,
          required = true
        )
      ),
      children = Map.empty,
      steps = Map.empty,
      orderBy = Seq.empty
    )


    "do nothing" in {
      val saver = new DataSaver
      saver.saveAll(model, None) must be equalTo JsArray()
    }

    "insert one row" in {
      val saver = new DataSaver with Database {
        override def insert(sql: String): ResultSet = {
          sql must be equalTo "INSERT INTO `person` (`name`) VALUES ('Foo')"
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

  }
}
