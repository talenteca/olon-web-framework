scalaVersion := "2.13.6"

libraryDependencies ++= {
  Seq(
    "com.talenteca"  %% "olon-webkit"     % "1.0.0"  % Compile,
    "ch.qos.logback" %  "logback-classic" % "1.0.6"  % Compile,
    "org.specs2"     %% "specs2-core"     % "4.10.0" % Test
  )
}

enablePlugins(JettyPlugin)
