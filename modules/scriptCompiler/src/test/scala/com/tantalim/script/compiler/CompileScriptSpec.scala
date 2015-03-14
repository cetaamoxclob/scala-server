package com.tantalim.script.compiler

import com.tantalim.models._
import com.tantalim.nodes.{TntString, SmartNodeSet}
import org.junit.runner.RunWith
import org.specs2.mutable._
import org.specs2.runner._

@RunWith(classOf[JUnitRunner])
class CompileScriptSpec extends Specification with FakeArtifacts {
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
  "Script" should {
    "do the basics" in {
      "printing" in {
        val script = """print ("Hello World")"""
        val interpreter = new TantalimScriptInterpreter(script)
        val result = interpreter.run()
        result must be equalTo Unit
      }
    }
    "models" in {
      "for" in {
        val people = SmartNodeSet(model)
        val person = people.insert
        // person.PersonName = "John Doe"
        val script = """for person in people { print(person) }"""
        new TantalimScriptInterpreter(script).run(Map("people" -> people))
        person.get("PersonName").get must be equalTo TntString("John Doe")
      }
    }
    "filter strings" in {

      "full syntax" in {
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

//        println(fullSyntax)
        node.rows.length must be equalTo 0
      }
    }
  }
}
