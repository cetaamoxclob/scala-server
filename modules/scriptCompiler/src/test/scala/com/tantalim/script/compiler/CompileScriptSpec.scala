package com.tantalim.script.compiler

import com.tantalim.models._
import com.tantalim.nodes.SmartNodeSet
import org.junit.runner.RunWith
import org.specs2.mutable._
import org.specs2.runner._

@RunWith(classOf[JUnitRunner])
class CompileScriptSpec extends Specification with FakeArtifacts {
  "Filter" should {
    "filter strings" in {
      "=" in {
        val model = new Model(
          "Person",
          basisTable = new ShallowTable("Person", "person"),
          fields = Map(
            fakeModelFieldMap("PersonID", "person_id", DataType.Integer, updateable = false),
            fakeModelFieldMap("PersonName", "name", required = true)
          ),
          children = Map("PersonPhone" -> new Model(
            "PersonPhone",
            basisTable = new ShallowTable("Phone", "phone"),
            fields = Map(
              fakeModelFieldMap("PersonPhoneID", "phone_id", DataType.Integer, updateable = false),
              fakeModelFieldMap("PersonPhonePersonID", "person_id", DataType.Integer, updateable = false),
              fakeModelFieldMap("PersonPhoneNumber", "phone_number", required = true)
            )
          ))
        )
        val node = SmartNodeSet(model)
        node.rows.length must be equalTo 0
      }
    }
  }
}
