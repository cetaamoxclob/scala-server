package core.artifacts

import com.tantalim.models.ArtifactType
import com.tantalim.util.TantalimException

class MissingArtifactException(artifactType: ArtifactType, artifactName: String)
  extends TantalimException(s"$artifactType ($artifactName) could not be found", "Add the artifact to source")
