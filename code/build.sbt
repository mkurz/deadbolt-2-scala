name := "deadbolt-scala"

organization := "be.objectify"

version := "2.3.3-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.11.1"

crossScalaVersions := Seq("2.11.1", "2.10.4")

libraryDependencies ++= Seq(
  cache,
  "be.objectify" %% "deadbolt-core" % "2.3.3"
)

resolvers += Resolver.sonatypeRepo("snapshots") 
