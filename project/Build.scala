import sbt._
import Keys._
import play.Play.autoImport._
import PlayKeys._

object ApplicationBuild extends Build {

  val appName         = "deadbolt-scala"
  val appVersion      = "2.3.0-RC1"

  val appDependencies = Seq(
    cache,
    "be.objectify" %% "deadbolt-core" % "2.3.0-RC1"
  )


  val main = Project(appName, file(".")).enablePlugins(play.PlayScala).settings(
    organization := "be.objectify",
    version := appVersion,
    libraryDependencies ++= appDependencies,
    resolvers += Resolver.url("Objectify Play Repository", url("http://schaloner.github.com/releases/"))(Resolver.ivyStylePatterns),
    resolvers += Resolver.url("Objectify Play Snapshot Repository", url("http://schaloner.github.com/snapshots/"))(Resolver.ivyStylePatterns)
  )
}
