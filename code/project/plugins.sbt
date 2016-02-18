// Comment to get more information during initialization
logLevel := Level.Warn

// The Play plugin 
addSbtPlugin("com.typesafe.play" % "sbt-plugin" % Option(System.getProperty("play.version")).getOrElse("2.5.0-RC1"))
