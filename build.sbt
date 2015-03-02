name := "tantalim"

version := "1.0"

lazy val `tantalim` = (project in file(".")).enablePlugins(PlayScala)
  .aggregate(tantalimServer).dependsOn(tantalimServer)
  .aggregate(tantalimModels).dependsOn(tantalimModels)

lazy val tantalimServer = (project in file("modules/tantalimServer")).enablePlugins(PlayScala)

lazy val tantalimModels = project in file("modules/tantalimModels")

scalaVersion := "2.11.1"

libraryDependencies ++= Seq( jdbc , anorm , cache , ws )

libraryDependencies += "mysql" % "mysql-connector-java" % "5.1.34"

unmanagedResourceDirectories in Test <+=  baseDirectory ( _ /"target/web/public/test" )
