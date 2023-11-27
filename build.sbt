val commonsSetting = Seq(
  crossScalaVersions := Seq("2.13.12", "3.3.1"),
  scalaVersion := crossScalaVersions.value.head,
  resolvers += "scalaz-bintray" at "https://dl.bintray.com/scalaz/releases",
)

val testSettings = Seq(
  libraryDependencies ++= Seq(
    ws % Test,
    specs2 % Test
  ),

  Test / parallelExecution := false,

  resolvers += Resolver.sonatypeRepo("snapshots"),
)

lazy val code = (project in file("code"))
  .settings(
    commonsSetting,
    name := "deadbolt-scala",

    organization := "be.objectify",

    libraryDependencies := libraryDependencies.value.filterNot(m => m.name == "twirl-api" || m.name == "play-server") ++ Seq(
      playCore % "provided",
      specs2 % Test
    ),

    Test / parallelExecution  := false,


    Test / fork := true,

    releasePublishArtifactsAction := PgpKeys.publishSigned.value
  ).enablePlugins(PlayScala).disablePlugins(PlayFilters, PlayLogback, PlayAkkaHttpServer)

lazy val `test-app` = (project in file("test-app"))
  .settings(
    commonsSetting,
    name := """test-app""",
    version := "2.9.0-SNAPSHOT",
    testSettings
  )
  .dependsOn(code)
  .enablePlugins(PlayScala)

lazy val `test-app-compile-time-di` = (project in file("test-app-compile-time-di"))
  .settings(
    commonsSetting,
    name := """test-app-compile-time-di""",
    version := "2.9.0-SNAPSHOT",
    testSettings
  )
  .dependsOn(code)
  .enablePlugins(PlayScala)

lazy val `test-app-route-comments` = (project in file("test-app-route-comments"))
  .settings(
    commonsSetting,
    name := """test-app-route-comments""",
    version := "2.9.0-SNAPSHOT",
    testSettings
  )
  .dependsOn(code)
  .enablePlugins(PlayScala)

lazy val root = (project in file(".")).settings(commonsSetting)
  .aggregate(code, `test-app`, `test-app-compile-time-di`)
  .settings(publish / aggregate := false)
