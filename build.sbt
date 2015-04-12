name := "tantalim"

version := "1.0"

lazy val app = (project in file("."))
  .enablePlugins(PlayScala)
  .aggregate(server, models, util, filterCompiler, scriptCompiler, nodes, core, database, artifacts)
  .dependsOn(server, core)

lazy val server = (project in file("modules/tantalimServer"))
  .enablePlugins(PlayScala)
  .dependsOn(core, models % "test->test;compile->compile")

lazy val core = (project in file("modules/core"))
  .enablePlugins(PlayScala)
  .dependsOn(filterCompiler, scriptCompiler, artifacts, database, models % "test->test;compile->compile")

lazy val artifacts = (project in file("modules/artifacts"))
  .dependsOn(models)

lazy val database = (project in file("modules/database")).dependsOn(nodes, filterCompiler, models % "test->test")

lazy val filterCompiler = (project in file("modules/filterCompiler"))
  .dependsOn(models % "test->test;compile->compile")

lazy val scriptCompiler = (project in file("modules/scriptCompiler"))
  .dependsOn(nodes, models % "test->test;compile->compile")

lazy val nodes = (project in file("modules/nodes"))
  .dependsOn(models)

lazy val models = (project in file("modules/tantalimModels"))
  .dependsOn(util)

lazy val util = project in file("modules/util")

scalaVersion := "2.11.1"

//resolvers += "Tantalim repository" at "https://github.com/tantalim/maven-repo/raw/master/"

//libraryDependencies += "com.tantalim" % "sample-admin" % "1.0"

libraryDependencies += "mysql" % "mysql-connector-java" % "5.1.34"

unmanagedResourceDirectories in Test <+= baseDirectory(_ / "target/web/public/test")
