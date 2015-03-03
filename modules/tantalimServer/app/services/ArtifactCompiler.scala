package services

import compiler.{MenuCompiler, ModelCompiler, PageCompiler}

class ArtifactCompilerService extends PageCompiler with ModelCompiler with MenuCompiler
