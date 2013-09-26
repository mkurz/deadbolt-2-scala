import sbt._
import Keys._
import play.Project._

object ApplicationBuild extends Build {

  val appName         = "deadbolt-scala"
  val appVersion      = "2.2-RC1"

  val appDependencies = Seq(
    cache,
    "be.objectify" %% "deadbolt-core" % "2.2-RC1"
  )


  val main = play.Project(appName, appVersion, appDependencies).settings(
    organization := "be.objectify",
    resolvers += Resolver.url("Objectify Play Repository", url("http://schaloner.github.com/releases/"))(Resolver.ivyStylePatterns),
    resolvers += Resolver.url("Objectify Play Snapshot Repository", url("http://schaloner.github.com/snapshots/"))(Resolver.ivyStylePatterns)
  )
}
