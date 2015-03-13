name := "scriptCompiler"

version := "1.0"

scalaVersion := "2.11.1"

libraryDependencies += "org.antlr" % "antlr4-runtime" % "4.5"

libraryDependencies += "org.specs2" %% "specs2-core" % "3.0.1" % "test"

libraryDependencies += "org.specs2" %% "specs2-junit" % "3.0.1" % "test"

resolvers += "scalaz-bintray" at "http://dl.bintray.com/scalaz/releases"

scalacOptions in Test ++= Seq("-Yrangepos")

