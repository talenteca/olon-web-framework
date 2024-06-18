lazy val root = project
  .in(file("."))
  .settings(
    name := "olon_basic_scala_2",
    description := "Example sbt project that compiles using Scala 2",
    version := "0.1.0",
    scalaVersion := "2.13.12",
    scalacOptions ++= Seq("-deprecation"),
    libraryDependencies ++= Seq(
      "com.talenteca" %% "olon-webkit" % "6.0.0-SNAPSHOT",
      "ch.qos.logback" % "logback-classic" % "1.4.11",
      "org.codehaus.janino" % "janino" % "3.1.8",
      "org.slf4j" % "log4j-over-slf4j" % "1.7.36",
      "org.slf4j" % "jcl-over-slf4j" % "1.7.36",
      "org.slf4j" % "jul-to-slf4j" % "1.7.36"
    ),
    Tomcat / containerLibs := Seq(
      "com.talenteca" % "canoa-webapp-runner" % "10.1.15" intransitive ()
    ),
    Tomcat / containerMain := "canoa.webapp.runner.launch.Main",
    Tomcat / javaOptions ++= Seq(
      "-noverify",
      "-Dtomcat.util.scan.StandardJarScanFilter.jarsToSkip=*.jar",
      "-Xss8m"
    )
  ).enablePlugins(TomcatPlugin)
