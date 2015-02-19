package services

import data.DataState
import models._
import org.junit.runner.RunWith
import org.specs2.mock._
import org.specs2.mutable._
import org.specs2.runner._
import play.api.libs.json.{Json, JsArray}

trait ArtifactCompilerMock extends ArtifactCompiler {
}

@RunWith(classOf[JUnitRunner])
class DataSaverSpec extends Specification with Mockito {
  "DataSaver" should {
    val saver = new DataSaver

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
          dataType = "",
          updateable = true,
          required = false
        )
      ),
      children = Map.empty,
      steps = Map.empty,
      orderBy = Seq.empty
    )

    "do nothing" in {
      saver.saveAll(model, None) must be equalTo JsArray()
    }

    "insert one row" in {
      val _fakeID = 1
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
