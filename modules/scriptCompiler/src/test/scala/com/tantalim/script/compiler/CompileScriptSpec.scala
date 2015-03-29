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
    basisTable = new DeepTable("Persons", "person", fakeModule()),
    fields = Map(
      fakeModelFieldMap("PersonID", "person_id", DataType.Integer, updateable = false),
      fakeModelFieldMap("PersonName", "name", required = true)
    )
  )
  model.addChild(new Model(
    "PersonPhone",
    basisTable = new DeepTable("Phone", "phone", fakeModule()),
    parent = Some(model),
    fields = Map(
      fakeModelFieldMap("PersonPhoneID", "phone_id", DataType.Integer, updateable = false),
      fakeModelFieldMap("PersonPhonePersonID", "person_id", DataType.Integer, updateable = false),
      fakeModelFieldMap("PersonPhoneNumber", "phone_number", required = true)
    )
  ))

  "Script" should {
    def runScriptWithResult(script: String, returnValue: Any) = {
      val interpreter = new TantalimScriptInterpreter(script)
      val result = interpreter.run()
      result must be equalTo returnValue
    }
    def runScriptWithUnit(script: String) = {
      runScriptWithResult(script, Unit)
    }

    "do the basics" in {

      "printing" in {
        val script = """print ("Hello World") """
        runScriptWithUnit(script)
      }
    }
    "atom" in {
      "1" in {
        val script = "return 1"
        runScriptWithResult(script, 1)
      }
      "1.234" in {
        val script = "return 1.234"
        runScriptWithResult(script, 1.234)
      }
      "foo" in {
        val script = """return "foo" """
        runScriptWithResult(script, "foo")
      }
      """("foo")""" in {
        val script = """return ("foo")"""
        runScriptWithResult(script, "foo")
      }
      "true" in {
        val script = "return true"
        runScriptWithResult(script, true)
      }
      "false" in {
        val script = "return false"
        runScriptWithResult(script, false)
      }
      "variables" in {
        val script =
          """
            |first = 2
            |return first
            |""".stripMargin
        runScriptWithResult(script, 2)
      }

    }
    "expressions" in {
      "parentheses" in {
        "1 + 1 + 1 = 3" in {
          val script = "return 1 + 1 + 1"
          runScriptWithResult(script, 3)
        }
        "(1 - 1) + 1 = 1" in {
          val script = "return (1 - 1) + 1"
          runScriptWithResult(script, 1)
        }
        "1 - (1 + 1) = -1" in {
          val script = "return 1 - (1 + 1)"
          runScriptWithResult(script, -1)
        }
      }
      "addition" in {
        "1 + 1 = 2" in {
          val script = "return 1 + 1"
          runScriptWithResult(script, 2)
        }
        "1.2 + 3 = 4.2" in {
          val script = "return 1.2 + 3"
          runScriptWithResult(script, 4.2)
        }
        "a + b = ab" in {
          val script = """return "a" + "b" """
          runScriptWithResult(script, "ab")
        }
      }
      "substraction" in {
        "10 - 3 = 7" in {
          val script = "return 10 - 3"
          runScriptWithResult(script, 7)
        }
        "3 - 5.2 = -2.2" in {
          val script = "return 3 - 5.2"
          runScriptWithResult(script, -2.2)
        }
      }
      "boolean" in {
        "or" in {
          val script = "return true or false"
          runScriptWithResult(script, true)
        }
        "and false" in {
          val script = "return true and false"
          runScriptWithResult(script, false)
        }
        "and true" in {
          val script = "return true and true"
          runScriptWithResult(script, true)
        }
      }
      "equality" in {
        "==" in {
          val script = "return 1 == 1"
          runScriptWithResult(script, true)
        }
      }
    }
    "if" in {
      "simple" in {
        val script = "if (true) return 1 else return 0"
        runScriptWithResult(script, 1)
      }
    }
    "models" in {
      "for" in {
        val people = SmartNodeSet(model)
        val person = people.insert
        val script = """for person in people { person.PersonName = "John Doe" }"""
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
