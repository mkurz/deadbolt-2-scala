val commonsSetting = Seq(
  crossScalaVersions := Seq("2.13.15", "3.3.3"),
  scalaVersion := crossScalaVersions.value.head,
  organization := "be.objectify",
  homepage := Some(url("https://github.com/mkurz/deadbolt-2-java")), // Some(url("http://deadbolt.ws"))
  licenses += ("Apache-2.0", url("https://www.apache.org/licenses/LICENSE-2.0.html")),
  publish / skip := true,
  scalacOptions ++= Seq("-release:11"),
)

val testSettings = Seq(
  libraryDependencies ++= Seq(
    ws % Test,
    specs2 % Test
  ),

  Test / parallelExecution := false,
)

// Customise sbt-dynver's behaviour to make it work with tags which aren't v-prefixed
ThisBuild / dynverVTagPrefix := false

// Sanity-check: assert that version comes from a tag (e.g. not a too-shallow clone)
// https://github.com/dwijnand/sbt-dynver/#sanity-checking-the-version
Global / onLoad := (Global / onLoad).value.andThen { s =>
  dynverAssertTagVersion.value
  s
}

lazy val code = (project in file("code"))
  .settings(
    commonsSetting,
    publish / skip := false, // override what commonsSetting just set
    name := "deadbolt-scala",

    libraryDependencies := libraryDependencies.value.filterNot(m => m.name == "twirl-api" || m.name == "play-server") ++ Seq(
      playCore % "provided",
      specs2 % Test
    ),

    Test / parallelExecution  := false,


    Test / fork := true,

    developers ++= List(Developer(
        "mkurz",
        "Matthias Kurz",
        "m.kurz@irregular.at",
        url("https://github.com/mkurz")
      ),
      Developer(
        "schaloner",
        "Steve Chaloner",
        "john.doe@example.com",
        url("https://github.com/schaloner")
      ),
    )
  ).enablePlugins(PlayScala).disablePlugins(PlayFilters, PlayLogback, PlayAkkaHttpServer)

lazy val `test-app` = (project in file("test-app"))
  .settings(
    commonsSetting,
    name := "test-app",
    testSettings
  )
  .dependsOn(code)
  .enablePlugins(PlayScala)

lazy val `test-app-compile-time-di` = (project in file("test-app-compile-time-di"))
  .settings(
    commonsSetting,
    name := "test-app-compile-time-di",
    testSettings
  )
  .dependsOn(code)
  .enablePlugins(PlayScala)

lazy val `test-app-route-comments` = (project in file("test-app-route-comments"))
  .settings(
    commonsSetting,
    name := "test-app-route-comments",
    testSettings
  )
  .dependsOn(code)
  .enablePlugins(PlayScala)

lazy val root = (project in file(".")).settings(commonsSetting)
  .aggregate(code, `test-app`, `test-app-compile-time-di`)
