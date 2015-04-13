package com.tantalim.artifacts

import com.tantalim.models.ArtifactType
import com.tantalim.util.TantalimException

class MissingArtifactException(artifactType: ArtifactType, artifactName: String)
  extends TantalimException(s"$artifactType ($artifactName) could not be found", s"Add the artifact to source. <a href='/page/BuildPage/?filter=PageName%20Equals%20%27$artifactName%27'>BuildPage</a>")
