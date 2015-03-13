name := "tantalim"

version := "1.0"

lazy val app = (project in file("."))
  .enablePlugins(PlayScala)
  .aggregate(server, models, util, filterCompiler, scriptCompiler, nodes)
  .dependsOn(server)

lazy val server = (project in file("modules/tantalimServer"))
  .enablePlugins(PlayScala)
  .dependsOn(models, util, filterCompiler, scriptCompiler, nodes)

lazy val models = project in file("modules/tantalimModels")

lazy val util = project in file("modules/util")

lazy val filterCompiler = (project in file("modules/filterCompiler"))
  .dependsOn(models, util)

lazy val scriptCompiler = (project in file("modules/scriptCompiler"))
  .dependsOn(nodes, models, util)

lazy val nodes = (project in file("modules/nodes"))
  .dependsOn(models, util)

scalaVersion := "2.11.1"

libraryDependencies += "mysql" % "mysql-connector-java" % "5.1.34"

unmanagedResourceDirectories in Test <+= baseDirectory(_ / "target/web/public/test")
