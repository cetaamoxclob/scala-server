package com.tantalim.artifacts

import com.tantalim.artifacts.compiler.{TableCompiler, MenuCompiler, ModelCompiler}
import com.tantalim.models._
import org.junit.runner._
import org.specs2.mutable._
import org.specs2.runner._
import play.api.libs.json.{JsValue, Json}

trait TableCacheMock extends TableCache {
  override def getTableFromCache(name: String): Option[DeepTable] = None

  override def addTableToCache(name: String, table: DeepTable): Unit = {}
}

@RunWith(classOf[JUnitRunner])
class ArtifactCompilerSpec extends Specification with FakeArtifacts {
  "ArtifactCompiler" should {
    "compile the Default menu" in {
      trait ArtifactServiceMock extends ArtifactService {
        override def getArtifactContentAndParseJson(artifactType: String, name: String): JsValue =
          Json.parse( """
{
  "appTitle": "Test App",
  "content": []
}
                      """)
      }

      val compilerService = new MenuCompiler with ArtifactServiceMock
      val menu = compilerService.compileMenu("Foo")
      menu.appTitle must be equalTo "Test App"
    }

    "compile the simple model" in {
      trait ArtifactServiceMock extends ArtifactService {
        override def getArtifactContentAndParseJson(artifactType: String, name: String): JsValue = {
          artifactType match {
            case TableCompiler.artifactName => Json.parse( """
{
  "dbName": "tbl_person",
  "columns": [{
    "name": "PersonID", "dbName": "person_id"
  }]
}""")
            case ModelCompiler.artifactName => Json.parse( """
{
  "basisTable": "Person",
  "fields": [{
    "name": "PersonID",
    "basisColumn": "PersonID"
  }]
}""")
            case _ => Json.parse("{}")
          }
        }
      }

      val expected = new Model(
        "ListPeople",
        basisTable = new DeepTable(
          "Person",
          "tbl_person",
          module = Module("Default", Database("Default", None)),
          primaryKey = None,
          columns = Map(fakeTableColumnMap("PersonID", "person_id"))
        ),
        fields = Map(fakeModelFieldMap("PersonID", "person_id", DataType.String)),
        steps = Map.empty,
        orderBy = Seq.empty
      )

      val compilerService = new ModelCompiler with ArtifactServiceMock with TableCacheMock
      val model = compilerService.compileModel("ListPeople")
      // TODO This is a bad test. The comparison doesn't work well
      model.toString must be equalTo expected.toString
    }
  }
}
