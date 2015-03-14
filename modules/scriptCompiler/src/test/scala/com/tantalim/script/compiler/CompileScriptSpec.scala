package com.tantalim.script.compiler

import com.tantalim.models._
import com.tantalim.nodes.SmartNodeSet
import org.junit.runner.RunWith
import org.specs2.mutable._
import org.specs2.runner._

@RunWith(classOf[JUnitRunner])
class CompileScriptSpec extends Specification with FakeArtifacts {
  "Script" should {
    "do the basics" in {
      "printing" in {
        val script = """print ("Hello World")"""
        val interpreter = new TantalimScriptInterpreter(script)
        val result = interpreter.run()
        result must be equalTo Unit
      }
    }
    "filter strings" in {
      val model = new Model(
        "Person",
        basisTable = new ShallowTable("Persons", "person"),
        fields = Map(
          fakeModelFieldMap("PersonID", "person_id", DataType.Integer, updateable = false),
          fakeModelFieldMap("PersonName", "name", required = true)
        ),
        children = Map("Phones" -> new Model(
          "PersonPhone",
          basisTable = new ShallowTable("Phone", "phone"),
          fields = Map(
            fakeModelFieldMap("PersonPhoneID", "phone_id", DataType.Integer, updateable = false),
            fakeModelFieldMap("PersonPhonePersonID", "person_id", DataType.Integer, updateable = false),
            fakeModelFieldMap("PersonPhoneNumber", "phone_number", required = true)
          )
        ))
      )
      "=" in {
        val node = SmartNodeSet(model)

        val fullSyntax =
          """
            |counter = 10
            |for person in Persons {
            |  for phone in person.PersonPhone {
            |    phone.PersonPhoneNumber = counter
            |    counter = counter + 10
            |  }
            |}
            |print ("counter = " + counter)
            |
            |firstPerson = Person.head
            |secondPerson = Person[1]
            |for phone in firstPerson.Phones {
            |  secondPhone = secondPerson.Phones.insert(phone.copy(
            |    PersonPhonePersonID = secondPerson.PersonID
            |  ))
            |  phone.delete
            |}
            |
            |firstPerson.delete
            |
            |Persons.save # You can only save if this is a rootset
            |managerFilter = "PersonTitle Contains 'Manager'"
            |managers = find Persons where managerFilter
            |
          """.replace("\r", "").stripMargin

        node.rows.length must be equalTo 0
      }
    }
  }
}
