package services

import java.io.File

import com.tantalim.artifacts.ArtifactService
import com.tantalim.artifacts.compiler.{MenuCompiler, ModelCompiler, PageCompiler, TableCompiler}
import com.tantalim.models.Module
import play.api.libs.json.{JsNumber, JsObject, Json}

import scala.collection.immutable.TreeMap

trait ArtifactLister {

  def getArtifactList: JsObject = {

    val artifactList = readArtifactList

    Json.obj(
      "maxPages" -> JsNumber(1),
      "rows" -> artifactList.map { case (moduleName, artifactTypes) =>
        Json.obj(
          "data" -> Json.obj("ModuleName" -> moduleName),
          "children" -> Json.obj("ArtifactTypes" -> artifactTypes.map { case (artifactType, artifactNames) =>
            Json.obj(
              "data" -> Json.obj("ArtifactType" -> artifactType),
              "children" -> Json.obj("ModuleArtifacts" -> artifactNames.map { artifactName =>
                Json.obj(
                  "data" -> Json.obj("ArtifactName" -> artifactName)
                )
              })
            )
          })
        )
      }
    )
  }

  private def getArtifactsByDir(moduleDir: File): Map[String, List[String]] = {
    // Maybe we should just read the directory for the list??
    val artifactTypes = Seq(TableCompiler.artifactName, ModelCompiler.artifactName,
      PageCompiler.artifactName, MenuCompiler.artifactName)

    artifactTypes.map { artifactType: String =>
      val typeDir = new File(moduleDir.getAbsolutePath + "/" + artifactType)
      val artifacts: List[String] = if (typeDir.isDirectory) {
        typeDir.listFiles().map { file =>
          file.getName.replace(".json", "")
        }.toList
      } else List.empty[String]
      artifactType.toString -> artifacts
    }.toMap.filter { case (_, artifacts) =>
      artifacts.nonEmpty
    }
  }

  private def readArtifactList: TreeMap[String, Map[String, List[String]]] = {
    val modules: Map[String, Map[String, List[String]]] = new File(ArtifactService.tantalimRoot + "/lib/").listFiles().map { file =>
      file.getName -> getArtifactsByDir(file)
    }.toMap
    val allModules = modules + (Module.default -> getArtifactsByDir(new File(ArtifactService.tantalimRoot + "/src/")))
    TreeMap(allModules.toArray: _*)
  }


}
