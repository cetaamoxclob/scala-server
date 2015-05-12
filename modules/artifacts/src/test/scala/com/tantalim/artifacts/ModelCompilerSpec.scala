package com.tantalim.artifacts

import com.tantalim.artifacts.compiler.ModelCompiler
import com.tantalim.artifacts.json.{ModelFieldJson, ModelJson, ModelStepJson}
import com.tantalim.models._
import org.junit.runner.RunWith
import org.specs2.mutable.Specification
import org.specs2.runner.JUnitRunner

@RunWith(classOf[JUnitRunner])
class ModelCompilerSpec extends Specification {

  val TEST = "test"
  val testDatabase = new Database(TEST, Some(TEST))
  val testModule = new Module(TEST, testDatabase)
  val PERSON = "Person"
  val shallowPersonTable = new ShallowTable(PERSON, PERSON.toLowerCase, testModule, columns = Map(
    "PersonID" -> new TableColumn("PersonID", "PersonID", dataType = DataType.Integer),
    "Name" -> new TableColumn("Name", "name"),
    "ParentID" -> new TableColumn("ParentID", "ParentID", dataType = DataType.Integer)
  ))
  val deepPersonTable = new DeepTable(
    shallowPersonTable.name, shallowPersonTable.dbName, shallowPersonTable.module, columns = shallowPersonTable.columns,
    joins = Map("Parent" -> new TableJoin("Parent", shallowPersonTable, required = true, columns = Seq.empty))
  )
  val compiler = new ModelCompiler with TableCache {
    override def getTableFromCache(name: String): Option[DeepTable] = Some(deepPersonTable)
  }

  val simpleModelJson = new ModelJson(
    PERSON,
    name = Some(PERSON),
    fields = None
  )


  "ModelCompiler" should {
    "compile simple" in {
      val model = compiler.compileModelView(simpleModelJson)
      model.basisTable.name must be equalTo "Person"
    }

    "compile fields" in {
      val modelJson = simpleModelJson.copy(
        fields = Some(Seq(
          new ModelFieldJson(
            "PersonPersonID", basisColumn = Some("PersonID")
          )
        ))
      )

      val model = compiler.compileModelView(modelJson)
      model.fields.get("PersonPersonID").isDefined must be equalTo true
    }

    "compile steps" in {
      val modelJson = simpleModelJson.copy(
        steps = Some(Seq(
          new ModelStepJson("Parent", "Parent")
        ))
      )

      val model = compiler.compileModelView(modelJson)
      model.steps.size must be equalTo 1
    }

    "compile steps with field" in {
      val modelJson = simpleModelJson.copy(
        steps = Some(Seq(
          new ModelStepJson("Parent", "Parent",
            fields = Some(Seq(
              new ModelFieldJson(
                "ParentPersonID", basisColumn = Some("PersonID")
              )
            )))
        ))
      )

      val model = compiler.compileModelView(modelJson)
      model.fields.get("ParentPersonID").isDefined must be equalTo true

      val field = model.fields.get("ParentPersonID").get
      field.step.isDefined must be equalTo true
    }

    "compile parent steps" in {
      val modelJson = simpleModelJson.copy(
        steps = Some(Seq(
          new ModelStepJson("Parent", "Parent",
            steps = Some(Seq(
              new ModelStepJson("Grandparent", "Parent",
                fields = Some(Seq(
                  new ModelFieldJson(
                    "GrandparentPersonID", basisColumn = Some("PersonID")
                  )
                )))
            ))
          )
        ))
      )

      val model = compiler.compileModelView(modelJson)
      model.steps.size must be equalTo 2
      model.steps.get(1).get.name must be equalTo "Parent"
      model.steps.get(2).get.name must be equalTo "Grandparent"
      model.fields.size must be equalTo 1
      model.fields.get("GrandparentPersonID").get.step.get.tableAlias must be equalTo 2
    }
  }
}
