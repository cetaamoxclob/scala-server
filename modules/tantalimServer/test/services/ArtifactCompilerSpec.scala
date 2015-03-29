package services

import com.tantalim.models._
import compiler.{MenuCompiler, ModelCompiler}
import org.junit.runner._
import org.specs2.mock._
import org.specs2.mutable._
import org.specs2.runner._
import play.api.libs.json.{JsValue, Json}

trait TableCacheMock extends TableCache {
  override def getTableFromCache(name: String): Option[DeepTable] = None

  override def addTableToCache(name: String, table: DeepTable): Unit = {}
}

@RunWith(classOf[JUnitRunner])
class ArtifactCompilerSpec extends Specification with Mockito with FakeArtifacts {
  "ArtifactCompiler" should {
    "compile the Default menu" in {
      trait ArtifactServiceMock extends ArtifactService {
        override def getArtifactContentAndParseJson(artifactType: ArtifactType, name: String): JsValue =
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
        override def getArtifactContentAndParseJson(artifactType: ArtifactType, name: String): JsValue = {
          artifactType match {
            case ArtifactType.Menu => Json.parse("{}")
            case ArtifactType.Page => Json.parse("{}")
            case ArtifactType.Table => Json.parse( """
{
  "dbName": "tbl_person",
  "columns": [{
    "name": "PersonID", "dbName": "person_id"
  }]
}""")
            case ArtifactType.Model => Json.parse( """
{
  "basisTable": "Person",
  "fields": [{
    "name": "PersonID",
    "basisColumn": "PersonID"
  }]
}""")

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
          columns = Map("PersonID" -> new TableColumn(
            "PersonID",
            "person_id",
            order = 0,
            dataType = DataType.String,
            updateable = true,
            required = false,
            label = "PersonID",
            fieldType = "text"
          )),
          joins = Map()
        ),
        limit = 0,
        parentLink = None,
        instanceID = None,
        fields = Map(fakeModelFieldMap("PersonID", "person_id", DataType.String)),
        steps = Map.empty,
        orderBy = Seq.empty
      )

      val compilerService = new ModelCompiler with ArtifactServiceMock with TableCacheMock
      val model = compilerService.compileModel("ListPeople")
      model must be equalTo expected
    }
  }
}
