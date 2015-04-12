name := "database"

version := "1.0"

scalaVersion := "2.11.1"

libraryDependencies ++= Seq(jdbc, cache)

libraryDependencies += "org.specs2" %% "specs2-core" % "3.0.1" % "test"

libraryDependencies += "org.specs2" %% "specs2-mock" % "3.0.1" % "test"

libraryDependencies += "org.specs2" %% "specs2-junit" % "3.0.1" % "test"
