name := "deadbolt-scala"

organization := "be.objectify"

version := "2.5.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.11.7"

libraryDependencies ++= Seq(
  cache,
  specs2 % Test,
  "be.objectify" %% "deadbolt-core" % "2.5.0-SNAPSHOT"
)

parallelExecution in Test := false

resolvers += "scalaz-bintray" at "http://dl.bintray.com/scalaz/releases"

resolvers += Resolver.sonatypeRepo("snapshots")


fork in Test := true
