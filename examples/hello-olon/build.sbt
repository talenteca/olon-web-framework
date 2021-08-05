name := "hello-olon"

version := "0.0.1"

organization := "Talenteca"

scalaVersion := "2.12.14"

scalacOptions ++= Seq("-deprecation", "-unchecked")

libraryDependencies ++= {
  Seq(
    "olon"           %% "olon-webkit"        % "1.0.0-SNAPSHOT"           % "compile",
    "olon"           %% "olon-mapper"        % "1.0.0-SNAPSHOT"           % "compile",
    "olon"           %% "olon-util"          % "1.0.0-SNAPSHOT"           % "compile",
    "olon"           %% "olon-json-ext"      % "1.0.0-SNAPSHOT"           % "compile",
    "javax.servlet" % "javax.servlet-api" % "4.0.1" % Provided withSources (),
    "ch.qos.logback"        % "logback-classic"     % "1.0.6",
    "org.specs2" %% "specs2-core" % "4.10.0" % Test withSources ()
  )
}

enablePlugins(JettyPlugin)
