Global / onChangedBuildSource := ReloadOnSourceChanges

Global / pgpSigningKey := Some("csaltos@talenteca.io")

ThisBuild / organization := "com.talenteca"
ThisBuild / version := "3.0.0-RC1"
ThisBuild / description := "Olon is a modern web framework based on the view first strategy (based on the Lift web framework)"
ThisBuild / homepage := Some(
  url("https://github.com/talenteca/olon-web-framework")
)
ThisBuild / licenses += ("Apache License, Version 2.0", url(
  "http://www.apache.org/licenses/LICENSE-2.0.txt"
))
ThisBuild / organizationName := "Talenteca"

lazy val versions = new {
  val scala3Version = "3.3.1"
  val scala2Version = "2.13.12"
  val specs2 = "4.15.0"
  val slf4jVersion = "1.7.36"
}

lazy val libs = new {

  type ModuleMap = String => ModuleID

  lazy val commons_codec = "commons-codec" % "commons-codec" % "1.11"

  lazy val commons_fileupload =
    "commons-fileupload" % "commons-fileupload" % "1.3.3"

  lazy val commons_httpclient =
    "commons-httpclient" % "commons-httpclient" % "3.1"

  lazy val jbcrypt = "org.mindrot" % "jbcrypt" % "0.4"

  lazy val joda_time = "joda-time" % "joda-time" % "2.10"

  lazy val joda_convert = "org.joda" % "joda-convert" % "2.1"

  lazy val htmlparser = "nu.validator" % "htmlparser" % "1.4.12"

  lazy val paranamer = "com.thoughtworks.paranamer" % "paranamer" % "2.8"

  lazy val scalap: ModuleMap = "org.scala-lang" % "scalap" % _

  lazy val scala_compiler: ModuleMap = "org.scala-lang" % "scala-compiler" % _

  lazy val slf4j_api = "org.slf4j" % "slf4j-api" % versions.slf4jVersion

  lazy val scala_parallel_collections =
    "org.scala-lang.modules" %% "scala-parallel-collections" % "0.2.0"

  lazy val scala_parser =
    "org.scala-lang.modules" %% "scala-parser-combinators" % "1.1.2"

  lazy val scala_xml = "org.scala-lang.modules" %% "scala-xml" % "2.1.0"

  lazy val rhino = "org.mozilla" % "rhino" % "1.7.10"

  lazy val xerces = "xerces" % "xercesImpl" % "2.11.0"

  lazy val logback = "ch.qos.logback" % "logback-classic" % "1.2.8" % Provided

  lazy val servlet_api =
    "javax.servlet" % "javax.servlet-api" % "3.1.0" % Provided

  lazy val jquery = "org.webjars.bower" % "jquery" % "1.11.3" % Provided

  lazy val jasmineCore =
    "org.webjars.bower" % "jasmine-core" % "2.4.1" % Provided

  lazy val jasmineAjax =
    "org.webjars.bower" % "jasmine-ajax" % "3.2.0" % Provided

  lazy val log4j = "log4j" % "log4j" % "1.2.17" % Provided

  lazy val jetty6 = "org.mortbay.jetty" % "jetty" % "6.1.26" % Test

  lazy val jwebunit =
    "net.sourceforge.jwebunit" % "jwebunit-htmlunit-plugin" % "2.5" % Test

  lazy val specs2 = "org.specs2" %% "specs2-core" % "4.15.0" % Test

  lazy val specs2Core =
    "org.specs2" %% "specs2-core" % versions.specs2 % Test withSources ()

  lazy val specs2Matchers =
    "org.specs2" %% "specs2-matcher-extra" % specs2.revision % Test withSources ()

  lazy val scalacheck =
    "org.specs2" %% "specs2-scalacheck" % specs2.revision % Test

  lazy val specs2Prov =
    "org.specs2" %% "specs2-core" % specs2.revision % Provided

  lazy val specs2MatchersProv =
    "org.specs2" %% "specs2-matcher-extra" % specs2.revision % Provided

  lazy val specs2Mock = "org.specs2" %% "specs2-mock" % specs2.revision % Test

  lazy val json4s = "org.json4s" %% "json4s-native" % "4.0.6" % Test
}

ThisBuild / scalaVersion := versions.scala2Version

ThisBuild / libraryDependencies ++= Seq(
  libs.specs2Core,
  libs.specs2Matchers,
  libs.specs2Mock,
  libs.scalacheck
)

ThisBuild / scalacOptions ++= Seq(
  "-deprecation",
  "-feature",
  "-language:implicitConversions",
  "-Ypatmat-exhaust-depth",
  "80",
  "-Ywarn-unused"
)

ThisBuild / scalafmtOnCompile := true

ThisBuild / scalafixOnCompile := true
ThisBuild / semanticdbEnabled := true
ThisBuild / semanticdbVersion := scalafixSemanticdb.revision

ThisBuild / scmInfo := Some(
  ScmInfo(
    url("https://github.com/talenteca/olon-web-framework"),
    "scm:git@github.com:talenteca/olon-web-framework.git"
  )
)
ThisBuild / developers := List(
  Developer(
    id = "csaltos",
    name = "Carlos Saltos",
    email = "csaltos@talenteca.io",
    url = url("https://csaltos.com/")
  )
)

// Remove all additional repository other than Maven Central from POM
ThisBuild / pomIncludeRepository := { _ => false }
ThisBuild / publishTo := {
  val nexus = "https://s01.oss.sonatype.org/"
  if (isSnapshot.value)
    Some("snapshots" at nexus + "content/repositories/snapshots")
  else Some("releases" at nexus + "service/local/staging/deploy/maven2")
}
ThisBuild / publishMavenStyle := true
ThisBuild / versionScheme := Some("semver-spec")

ThisBuild / credentials += {
  if (new java.io.File(".credentials").exists()) {
    Credentials(file(".credentials"))
  } else {
    Credentials(
      "Sonatype Nexus Repository Manager",
      "s01.oss.sonatype.org",
      "ignore",
      "ignore"
    )
  }
}

def commonProjectSettings = {
  Defaults.coreDefaultSettings ++ Seq(
    crossScalaVersions := Seq(versions.scala2Version, versions.scala3Version),
    autoAPIMappings := true,
    apiMappings ++= {
      val cp: Seq[Attributed[File]] = (Compile / fullClasspath).value
      val apiUrlSeq = cp map { entry =>
        entry.get(moduleID.key) match {
          case Some(moduleInfo) =>
            findApiUrl(moduleInfo, scalaBinaryVersion.value) match {
              case Some(apiUrl) =>
                Some((entry.data -> apiUrl))
              case None =>
                None
            }
          case None =>
            None
        }
      }
      apiUrlSeq.filter(_.nonEmpty).map(_.get).toMap
    }
  )
}

lazy val root = Project(id = "root", base = file("."))
  .settings(
    publish := false,
    publishLocal := false
  )
  .aggregate(
    olon_common,
    olon_actor,
    olon_json,
    olon_json_ext,
    olon_util,
    olon_testkit,
    olon_webkit
  )

lazy val olon_common = Project(id = "olon-common", base = file("olon-common"))
  .settings(commonProjectSettings)
  .settings(
    description := "Common Libraries and Utilities",
    libraryDependencies ++= Seq(
      libs.slf4j_api,
      libs.logback,
      libs.log4j,
      libs.scala_xml,
      libs.scala_parser
    )
  )

lazy val olon_actor = Project("olon-actor", file("olon-actor"))
  .settings(commonProjectSettings)
  .settings(
    description := "Simple Actor",
    Test / parallelExecution := false
  )
  .dependsOn(olon_common)

lazy val olon_json = Project("olon-json", file("olon-json"))
  .settings(commonProjectSettings)
  .settings(
    description := "JSON Library",
    Test / parallelExecution := false,
    libraryDependencies ++= Seq(
      libs.scalap(scalaVersion.value),
      libs.paranamer,
      libs.scala_xml,
      libs.json4s
    )
  )

lazy val olon_json_ext = Project("olon-json-ext", file("olon-json-ext"))
  .settings(commonProjectSettings)
  .settings(
    description := "Extentions to JSON Library",
    libraryDependencies ++= Seq(
      libs.commons_codec,
      libs.joda_time,
      libs.joda_convert
    )
  )
  .dependsOn(olon_common, olon_json)

lazy val olon_util = Project("olon-util", file("olon-util"))
  .settings(commonProjectSettings)
  .settings(
    description := "Utilities Library",
    Test / parallelExecution := false,
    libraryDependencies ++= Seq(
      libs.scala_compiler(scalaVersion.value),
      libs.joda_time,
      libs.joda_convert,
      libs.commons_codec,
      libs.htmlparser,
      libs.xerces,
      libs.jbcrypt
    )
  )
  .dependsOn(olon_actor, olon_json)

lazy val olon_testkit = Project("olon-testkit", file("olon-testkit"))
  .settings(commonProjectSettings)
  .settings(
    description := "Testkit for Webkit Library",
    libraryDependencies ++= Seq(libs.commons_httpclient, libs.servlet_api)
  )
  .dependsOn(olon_util)

lazy val olon_webkit = Project("olon-webkit", file("olon-webkit"))
  .settings(commonProjectSettings)
  .settings(
    description := "Webkit Library",
    Test / parallelExecution := false,
    libraryDependencies ++= Seq(
      libs.commons_fileupload,
      libs.rhino,
      libs.servlet_api,
      libs.specs2Prov,
      libs.specs2MatchersProv,
      libs.jetty6,
      libs.jwebunit,
      libs.jquery,
      libs.jasmineCore,
      libs.jasmineAjax
    ),
    libraryDependencies ++= {
      CrossVersion.partialVersion(scalaVersion.value) match {
        case Some((2, scalaMajor)) if scalaMajor >= 13 =>
          Seq(libs.scala_parallel_collections)
        case _ => Seq.empty
      }
    },
    Test / initialize := {
      System.setProperty(
        "olon.webapptest.src.test.webapp",
        ((Test / sourceDirectory).value / "webapp").absString
      )
    },
    Compile / unmanagedSourceDirectories += {
      (Compile / sourceDirectory).value / ("scala_" + scalaBinaryVersion.value)
    },
    Test / unmanagedSourceDirectories += {
      (Test / sourceDirectory).value / ("scala_" + scalaBinaryVersion.value)
    },
    Compile / compile := (Compile / compile).dependsOn(WebKeys.assets).value,
    /** This is to ensure that the tests in olon.webapptest run last so that
      * other tests (MenuSpec in particular) run before the SiteMap is set.
      */
    Test / testGrouping := {
      (Test / definedTests).map { tests =>
        import Tests._

        val (webapptests, others) = tests.partition { test =>
          test.name.startsWith("olon.webapptest")
        }

        Seq(
          new Group("others", others, InProcess),
          new Group("webapptests", webapptests, InProcess)
        )
      }.value
    }
  )
  .dependsOn(olon_util, olon_testkit % Provided)
  .enablePlugins(SbtWeb)

def findApiUrl(
    moduleInfo: ModuleID,
    scalaBinaryVersionString: String
): Option[URL] = {
  if (moduleInfo.organization == "com.talenteca") {
    None
  } else {
    Some(
      url(
        "https://www.javadoc.io/doc/" + moduleInfo.organization + "/" + moduleInfo.name + "/" + moduleInfo.revision + "/"
      )
    )
  }
}
