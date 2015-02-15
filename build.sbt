name := "tantalim"

version := "1.0"

lazy val `tantalim` = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.11.1"

libraryDependencies ++= Seq( jdbc , anorm , cache , ws )

libraryDependencies += "mysql" % "mysql-connector-java" % "5.1.34"

unmanagedResourceDirectories in Test <+=  baseDirectory ( _ /"target/web/public/test" )
