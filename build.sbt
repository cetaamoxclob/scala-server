name := "tantalim"

version := "1.0"

lazy val app = (project in file("."))
  .enablePlugins(PlayScala)
  .aggregate(server, models, util, filterCompiler, scriptCompiler, nodes)
  .dependsOn(server)

lazy val server = (project in file("modules/tantalimServer"))
  .enablePlugins(PlayScala)
  .dependsOn(filterCompiler, scriptCompiler, models % "test->test;compile->compile")

lazy val filterCompiler = (project in file("modules/filterCompiler"))
  .dependsOn(models % "test->test;compile->compile", util)

lazy val scriptCompiler = (project in file("modules/scriptCompiler"))
  .dependsOn(nodes)

lazy val nodes = (project in file("modules/nodes"))
  .dependsOn(models, util)

lazy val models = project in file("modules/tantalimModels")

lazy val util = project in file("modules/util")

scalaVersion := "2.11.1"

libraryDependencies += "mysql" % "mysql-connector-java" % "5.1.34"

unmanagedResourceDirectories in Test <+= baseDirectory(_ / "target/web/public/test")
