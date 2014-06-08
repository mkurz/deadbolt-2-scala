name := "deadbolt-scala"

organization := "be.objectify"

version := "2.3.0-RC1"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.11.1"

libraryDependencies ++= Seq(
  cache,
  "be.objectify" %% "deadbolt-core" % "2.3.0-RC1"
)

resolvers += Resolver.url("Objectify Play Repository", url("http://schaloner.github.io/releases/"))(Resolver.ivyStylePatterns)
