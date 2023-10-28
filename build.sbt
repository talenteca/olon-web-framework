import Dependencies._
import OlonSbtHelpers._

Global / onChangedBuildSource := ReloadOnSourceChanges

Global / pgpSigningKey := Some("csaltos@talenteca.io")

ThisBuild / organization := "com.talenteca"
ThisBuild / version := "2.0.0"
ThisBuild / description := "Olon is a modern web framework based on the view first strategy (based on the Lift web framework)"
ThisBuild / homepage := Some(
  url("https://github.com/talenteca/olon-web-framework")
)
ThisBuild / licenses += ("Apache License, Version 2.0", url(
  "http://www.apache.org/licenses/LICENSE-2.0.txt"
))
ThisBuild / organizationName := "Talenteca"

val scala3Version = "3.1.3"
val scala2Version = "2.13.8"

val crossUpVersions = Seq(scala2Version, scala3Version)

ThisBuild / scalaVersion := scala2Version
ThisBuild / crossScalaVersions := crossUpVersions

ThisBuild / libraryDependencies ++= Seq(
  specs2,
  specs2Matchers,
  specs2Mock,
  scalacheck
)

ThisBuild / scalacOptions ++= Seq(
  "-deprecation",
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
ThisBuild / credentials += Credentials(file(".credentials"))

lazy val olonProjects = core ++ web

lazy val root =
  (project in file("."))
    .settings(
      publish := false,
      publishLocal := false
    )
    .aggregate(olonProjects: _*)

lazy val core: Seq[ProjectReference] =
  Seq(common, actor, json, json_ext, util)

lazy val common =
  coreProject("common")
    .settings(
      description := "Common Libraries and Utilities",
      libraryDependencies ++= Seq(
        slf4j_api,
        logback,
        log4j,
        scala_xml,
        scala_parser
      )
    )
    .settings(crossScalaVersions := crossUpVersions)

lazy val actor =
  coreProject("actor")
    .dependsOn(common)
    .settings(
      description := "Simple Actor",
      Test / parallelExecution := false
    )
    .settings(crossScalaVersions := crossUpVersions)

lazy val json =
  coreProject("json")
    .settings(
      description := "JSON Library",
      Test / parallelExecution := false,
      libraryDependencies ++= Seq(
        scalap(scalaVersion.value),
        paranamer,
        scala_xml,
        json4s
      )
    )
    .settings(crossScalaVersions := crossUpVersions)

lazy val json_ext =
  coreProject("json-ext")
    .dependsOn(common, json)
    .settings(
      description := "Extentions to JSON Library",
      libraryDependencies ++= Seq(commons_codec, joda_time, joda_convert)
    )
    .settings(crossScalaVersions := crossUpVersions)

lazy val util =
  coreProject("util")
    .dependsOn(actor, json)
    .settings(
      description := "Utilities Library",
      Test / parallelExecution := false,
      libraryDependencies ++= Seq(
        scala_compiler(scalaVersion.value),
        joda_time,
        joda_convert,
        commons_codec,
        htmlparser,
        xerces,
        jbcrypt
      )
    )
    .settings(crossScalaVersions := crossUpVersions)

lazy val web: Seq[ProjectReference] =
  Seq(testkit, webkit)

lazy val testkit =
  webProject("testkit")
    .dependsOn(util)
    .settings(
      description := "Testkit for Webkit Library",
      libraryDependencies ++= Seq(commons_httpclient, servlet_api)
    )
    .settings(crossScalaVersions := crossUpVersions)

lazy val webkit =
  webProject("webkit")
    .dependsOn(util, testkit % "provided")
    .settings(
      description := "Webkit Library",
      Test / parallelExecution := false,
      libraryDependencies ++= Seq(
        commons_fileupload,
        rhino,
        servlet_api,
        specs2Prov,
        specs2MatchersProv,
        jetty6,
        jwebunit,
        jquery,
        jasmineCore,
        jasmineAjax
      ),
      libraryDependencies ++= {
        CrossVersion.partialVersion(scalaVersion.value) match {
          case Some((2, scalaMajor)) if scalaMajor >= 13 =>
            Seq(scala_parallel_collections)
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
    .enablePlugins(SbtWeb)
    .settings(crossScalaVersions := crossUpVersions)
