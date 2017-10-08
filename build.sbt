val commonsSetting = Seq(
  scalaVersion := "2.11.11",
  version := "2.6.1-SNAPSHOT",
  resolvers += "scalaz-bintray" at "http://dl.bintray.com/scalaz/releases"
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


    crossScalaVersions := Seq("2.11.11", "2.12.2"),

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
    crossScalaVersions := Seq("2.11.11", "2.12.2"),
    testSettings
  )
  .dependsOn(code)
  .enablePlugins(PlayScala)

lazy val `test-app-compile-time-di` = (project in file("test-app-compile-time-di"))
  .settings(
    commonsSetting,
    name := """test-app-compile-time-di""",
    testSettings
  )
  .dependsOn(code)
  .enablePlugins(PlayScala)

lazy val `test-app-route-comments` = (project in file("test-app-route-comments"))
  .settings(
    commonsSetting,
    name := """test-app-route-comments""",
    testSettings
  )
  .dependsOn(code)
  .enablePlugins(PlayScala)

lazy val root = (project in file(".")).settings(commonsSetting)
  .aggregate(code, `test-app`, `test-app-compile-time-di`)
  .settings(aggregate in publish := false)



