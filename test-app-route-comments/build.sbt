name := """test-app-route-comments"""

version := "2.5.1-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.11.7"

libraryDependencies ++= Seq(
  "be.objectify" %% "deadbolt-scala" % "2.5.1-SNAPSHOT",
  ws % Test,
  specs2 % Test
)

parallelExecution in Test := false

resolvers += "scalaz-bintray" at "http://dl.bintray.com/scalaz/releases"

resolvers += Resolver.sonatypeRepo("snapshots")

routesGenerator := InjectedRoutesGenerator
