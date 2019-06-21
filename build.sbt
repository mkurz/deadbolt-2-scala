val commonsSetting = Seq(
  scalaVersion := "2.12.8",
  crossScalaVersions := Seq("2.11.12", "2.12.8", "2.13.0"),
  resolvers += "scalaz-bintray" at "http://dl.bintray.com/scalaz/releases",

  // Workaround until omnidoc gets published for Scala 2.13
  // http://central.maven.org/maven2/com/typesafe/play/play-omnidoc_2.13/
  PlayKeys.playOmnidoc := false
)

val testSettings = Seq(
  libraryDependencies ++= Seq(
    ws % Test,
    specs2 % Test
  ),

  parallelExecution in Test := false,

  resolvers += Resolver.sonatypeRepo("snapshots"),

  routesGenerator := InjectedRoutesGenerator
)

lazy val code = (project in file("code"))
  .settings(
    commonsSetting,
    name := "deadbolt-scala",

    organization := "be.objectify",

    libraryDependencies ++= Seq(
      specs2 % Test
    ),

    parallelExecution in Test := false,


    fork in Test := true,

    releasePublishArtifactsAction := PgpKeys.publishSigned.value
  ).enablePlugins(PlayScala).disablePlugins(PlayFilters)

lazy val `test-app` = (project in file("test-app"))
  .settings(
    commonsSetting,
    name := """test-app""",
    version := "2.7.1-SNAPSHOT",
    testSettings
  )
  .dependsOn(code)
  .enablePlugins(PlayScala)

lazy val `test-app-compile-time-di` = (project in file("test-app-compile-time-di"))
  .settings(
    commonsSetting,
    name := """test-app-compile-time-di""",
    version := "2.7.1-SNAPSHOT",
    testSettings
  )
  .dependsOn(code)
  .enablePlugins(PlayScala)

lazy val `test-app-route-comments` = (project in file("test-app-route-comments"))
  .settings(
    commonsSetting,
    name := """test-app-route-comments""",
    version := "2.7.1-SNAPSHOT",
    testSettings
  )
  .dependsOn(code)
  .enablePlugins(PlayScala)

lazy val root = (project in file(".")).settings(commonsSetting)
  .aggregate(code, `test-app`, `test-app-compile-time-di`)
  .settings(aggregate in publish := false)
