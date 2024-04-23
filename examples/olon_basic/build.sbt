lazy val root = project
  .in(file("."))
  .settings(
    name := "scala3-example-project",
    description := "Example sbt project that compiles using Scala 3",
    version := "0.1.0",
    scalaVersion := "3.4.1-RC1",
    scalacOptions ++= Seq("-deprecation"),
    libraryDependencies ++= Seq(
      "com.talenteca" %% "olon-webkit" % "6.0.0-SNAPSHOT"
      )
  ).enablePlugins(TomcatPlugin)
