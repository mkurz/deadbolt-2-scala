// Comment to get more information during initialization
logLevel := Level.Warn

// The Play plugin
addSbtPlugin("com.typesafe.play" % "sbt-plugin" % Option(System.getProperty("play.version")).getOrElse("2.9.0"))
addSbtPlugin("com.jsuereth" % "sbt-pgp" % "1.1.2-1")
addSbtPlugin("com.github.gseitz" % "sbt-release" % "1.0.13")
