package com.tantalim.artifacts

import java.io.File
import java.nio.charset.StandardCharsets

import com.google.common.base.Charsets
import com.google.common.io.Files
import com.tantalim.models.Module
import com.tantalim.util.TantalimException
import play.api.Play
import play.api.libs.json._

trait ArtifactService {

  case class SourceLocation(filePath: String, module: String)

  private def getSourceLocation(artifactType: String, name: String): SourceLocation = {
    val fileNameAndPartialDirLocation = artifactType + "/" + name + ".json"
    val srcDirLocation = ArtifactService.tantalimRoot + "/src/"
    val srcDir = new File(srcDirLocation)
    if (!srcDir.exists()) {
      println("Creating Tantalim Lib Directory")
      srcDir.mkdir()
    }

    val srcFileLocation = srcDirLocation + fileNameAndPartialDirLocation

    if (fileExists(srcFileLocation)) SourceLocation(srcFileLocation, Module.default)
    else {
      val libDirLocation = ArtifactService.tantalimRoot + "/lib/"
      val libDir = new File(libDirLocation)
      if (!libDir.exists()) {
        println("Creating Tantalim Lib Directory")
        libDir.mkdir()
      }
      val libList: Array[SourceLocation] = libDir.listFiles.flatMap(d => {
        val libSrcDir = d.toString + "/" + fileNameAndPartialDirLocation
        val moduleName = d.toString.substring(libDirLocation.length)
        if (d.isDirectory && fileExists(libSrcDir)) Some(SourceLocation(filePath = libSrcDir, module = moduleName))
        else None
      })

      if (libList.length == 0) {
        throw new MissingArtifactException(artifactType, name)
      }
      if (libList.length > 1) {
        println(s"WARN: $artifactType $name was found in more than one location. Using the 'first'")
      }

      libList.headOption.get
    }
  }

  private def fileExists(fileLocation: String): Boolean = {
    val f = new File(fileLocation)
    f.exists() && f.isFile
  }

  def getArtifactContentAndParseJson(artifactType: String, name: String): JsValue = {
    val sourceLocation = getSourceLocation(artifactType, name)
    val artifactContent = Files.toString(new File(sourceLocation.filePath), Charsets.UTF_8)

    if (artifactContent.isEmpty)
      throw new TantalimException(s"Artifact $artifactType named `$name` is empty", "Edit the file: " + sourceLocation.filePath + artifactContent)

    try {
      val result = Json.parse(artifactContent).asInstanceOf[JsObject]
      result + ("module" -> JsString(sourceLocation.module))
    } catch {
      case e: Exception => throw new TantalimException(
        s"Failed to parse json for $artifactType named `$name`",
        e.getMessage
      )
    }
  }
}

object ArtifactService {
  val charSet = StandardCharsets.UTF_8
  val tantalimRoot = {
    val config = Play.current.configuration.getString("tantalim.source")
    if (config.isDefined) config.get else "tantalim"
  }
}
