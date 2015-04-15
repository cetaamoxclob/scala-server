package com.tantalim.artifacts

import com.tantalim.models.ArtifactType
import com.tantalim.util.TantalimException

class MissingArtifactException(artifactType: ArtifactType, artifactName: String)
  extends TantalimException(
    s"$artifactType ($artifactName) could not be found",
    "Add the artifact to source. " + MissingArtifactException.getLink(artifactType, artifactName)
  )

object MissingArtifactException {
  def getLink(artifactType: ArtifactType, artifactName: String) = {
    val (pageName, fieldName) = artifactType match {
      case ArtifactType.Page => ("BuildPage", "PageName")
      case ArtifactType.Menu => ("BuildMenu", "MenuName")
      case ArtifactType.Model => ("BuildModel", "ModelName")
      case ArtifactType.Table => ("BuildTable", "TableName")
    }
    s"<a href='/page/$pageName/?filter=$fieldName Equals %27$artifactName%27'>$pageName</a>"
  }
}
