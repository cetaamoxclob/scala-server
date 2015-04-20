package com.tantalim.artifacts

import com.tantalim.artifacts.compiler.{TableCompiler, ModelCompiler, MenuCompiler, PageCompiler}
import com.tantalim.util.TantalimException

class MissingArtifactException(artifactType: String, artifactName: String)
  extends TantalimException(
    s"$artifactType ($artifactName) could not be found",
    "Add the artifact to source. " + MissingArtifactException.getLink(artifactType, artifactName)
  )

object MissingArtifactException {
  def getLink(artifactType: String, artifactName: String) = {
    val (pageName, fieldName) = artifactType match {
      case PageCompiler.artifactName => ("BuildPage", "PageName")
      case MenuCompiler.artifactName => ("BuildMenu", "MenuName")
      case ModelCompiler.artifactName => ("BuildModel", "ModelName")
      case TableCompiler.artifactName => ("BuildTable", "TableName")
      case _ => ("", "")
    }
    if (pageName.isEmpty) ""
    else s"<a href='/page/$pageName/?filter=$fieldName Equals %27$artifactName%27'>$pageName</a>"
  }
}
