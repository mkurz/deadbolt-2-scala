name := "deadbolt-scala"

organization := "be.objectify"

version := "2.4.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(play.PlayScala)

scalaVersion := "2.11.5"

crossScalaVersions := Seq("2.11.5", "2.10.4")

libraryDependencies ++= Seq(
  cache,
  specs2 % Test,
  "be.objectify" %% "deadbolt-core" % "2.4.0-SNAPSHOT"
)

resolvers += Resolver.sonatypeRepo("snapshots") 
