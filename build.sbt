name := "tantalim"

version := "1.0"

lazy val app = (project in file(".")).enablePlugins(PlayScala)
  .aggregate(server, models, util).dependsOn(server, models, util)

lazy val server = (project in file("modules/tantalimServer")).enablePlugins(PlayScala).dependsOn(models)

lazy val models = project in file("modules/tantalimModels")

lazy val util = project in file("modules/util")

scalaVersion := "2.11.1"

libraryDependencies += "mysql" % "mysql-connector-java" % "5.1.34"

unmanagedResourceDirectories in Test <+= baseDirectory(_ / "target/web/public/test")
