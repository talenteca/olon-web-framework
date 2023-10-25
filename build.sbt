import Dependencies._
import OlonSbtHelpers._

ThisBuild / organization := "com.talenteca"
ThisBuild / version := "1.0.0"
ThisBuild / description := "A derivative work of Lift focused on new updates and security recommendations."
ThisBuild / homepage := Some(url("https://github.com/talenteca/olon-web-framework"))
ThisBuild / licenses += ("Apache License, Version 2.0", url("http://www.apache.org/licenses/LICENSE-2.0.txt"))
ThisBuild / organizationName := "Talenteca"

val scala212Version = "2.12.18"
val scala213Version = "2.13.12"

val crossUpTo213 = Seq(scala213Version, scala212Version)

ThisBuild / scalaVersion := scala213Version
ThisBuild / crossScalaVersions := crossUpTo213

ThisBuild / libraryDependencies ++= Seq(specs2, specs2Matchers, specs2Mock, scalacheck, scalactic, scalatest)

ThisBuild / scalacOptions ++= Seq("-deprecation")

ThisBuild / scmInfo := Some(
  ScmInfo(
    url("https://github.com/talenteca/olon-web-framework"),
    "scm:git@github.com:talenteca/olon-web-framework.git"
  )
)
ThisBuild / developers := List(
  Developer(
    id    = "csaltos",
    name  = "Carlos Saltos",
    email = "csaltos@talenteca.io",
    url   = url("https://csaltos.com/")
  )
)

// Remove all additional repository other than Maven Central from POM
ThisBuild / pomIncludeRepository := { _ => false }
ThisBuild / publishTo := {
  val nexus = "https://s01.oss.sonatype.org/"
  if (isSnapshot.value) Some("snapshots" at nexus + "content/repositories/snapshots")
  else Some("releases" at nexus + "service/local/staging/deploy/maven2")
}
ThisBuild / publishMavenStyle := true


lazy val olonProjects = core ++ web ++ persistence

lazy val root =
  (project in file("."))
  .settings(
    publish := false,
    publishLocal := false
  )
  .aggregate(olonProjects: _*)

// Core Projects
// -------------
lazy val core: Seq[ProjectReference] =
  Seq(common, actor, markdown, json, json_scalaz7, json_ext, util)

lazy val common =
  coreProject("common")
    .settings(
      description := "Common Libraties and Utilities",
      libraryDependencies ++= Seq(slf4j_api, logback, slf4j_log4j12, scala_xml, scala_parser)
    )
    .settings(crossScalaVersions := crossUpTo213)

lazy val actor =
  coreProject("actor")
    .dependsOn(common)
    .settings(
      description := "Simple Actor",
      Test / parallelExecution := false
    )
    .settings(crossScalaVersions := crossUpTo213)

lazy val markdown =
  coreProject("markdown")
    .settings(
      description := "Markdown Parser",
      Test / parallelExecution := false,
      libraryDependencies ++= Seq(scalatest, scalatest_junit, scala_xml, scala_parser)
    )
    .settings(crossScalaVersions := crossUpTo213)

lazy val json =
  coreProject("json")
    .settings(
      description := "JSON Library",
      Test / parallelExecution := false,
      libraryDependencies ++= Seq(scalap(scalaVersion.value), paranamer,  scala_xml)
    )
    .settings(crossScalaVersions := crossUpTo213)

lazy val documentationHelpers =
  coreProject("documentation-helpers")
    .settings(description := "Documentation Helpers")
    .dependsOn(util)
    .settings(crossScalaVersions := crossUpTo213)

lazy val json_scalaz7 =
  coreProject("json-scalaz7")
    .dependsOn(json)
    .settings(
      description := "JSON Library based on Scalaz 7",
      libraryDependencies ++= Seq(scalaz7)
    )
    .settings(crossScalaVersions := crossUpTo213)

lazy val json_ext =
  coreProject("json-ext")
    .dependsOn(common, json)
    .settings(
      description := "Extentions to JSON Library",
      libraryDependencies ++= Seq(commons_codec, joda_time, joda_convert)
    )
    .settings(crossScalaVersions := crossUpTo213)

lazy val util =
  coreProject("util")
    .dependsOn(actor, json, markdown)
    .settings(
      description := "Utilities Library",
      Test / parallelExecution := false,
      libraryDependencies ++= Seq(
        scala_compiler(scalaVersion.value),
        joda_time,
        joda_convert,
        commons_codec,
        javamail,
        log4j,
        htmlparser,
        xerces,
        jbcrypt
      )
    )
    .settings(crossScalaVersions := crossUpTo213)

// Web Projects
// ------------
lazy val web: Seq[ProjectReference] =
  Seq(testkit, webkit)

lazy val testkit =
  webProject("testkit")
    .dependsOn(util)
    .settings(
      description := "Testkit for Webkit Library",
      libraryDependencies ++= Seq(commons_httpclient, servlet_api)
    )
    .settings(crossScalaVersions := crossUpTo213)

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
        mockito_scalatest,
        jquery,
        jasmineCore,
        jasmineAjax
      ),
      libraryDependencies ++= {
        CrossVersion.partialVersion(scalaVersion.value) match {
          case Some((2, scalaMajor)) if scalaMajor >= 13 => Seq(scala_parallel_collections)
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
      /**
        * This is to ensure that the tests in olon.webapptest run last
        * so that other tests (MenuSpec in particular) run before the SiteMap
        * is set.
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
      },

    )
    .enablePlugins(SbtWeb)
    .settings(crossScalaVersions := crossUpTo213)

// Persistence Projects
// --------------------
lazy val persistence: Seq[ProjectReference] =
  Seq(db, proto, mapper, record, mongodb, mongodb_record)

lazy val db =
  persistenceProject("db")
    .dependsOn(util, webkit)
    .settings(libraryDependencies += mockito_scalatest)
    .settings(crossScalaVersions := crossUpTo213)

lazy val proto =
  persistenceProject("proto")
    .dependsOn(webkit)
    .settings(crossScalaVersions := crossUpTo213)

lazy val mapper =
  persistenceProject("mapper")
    .dependsOn(db, proto)
    .settings(
      description := "Mapper Library",
      Test / parallelExecution := false,
      libraryDependencies ++= Seq(h2, derby, jbcrypt),
      Test / initialize := {
        System.setProperty(
          "derby.stream.error.file",
          ((Test / crossTarget).value / "derby.log").absolutePath
        )
      }
    )
    .settings(crossScalaVersions := crossUpTo213)

lazy val record =
  persistenceProject("record")
    .dependsOn(proto)
    .settings(libraryDependencies ++= Seq(jbcrypt))
    .settings(crossScalaVersions := crossUpTo213)

lazy val mongodb =
  persistenceProject("mongodb")
    .dependsOn(json_ext, util)
    .settings(
      crossScalaVersions := crossUpTo213,
      Test / parallelExecution := false,
      libraryDependencies ++= Seq(mongo_java_driver, mongo_java_driver_async),
      Test / initialize := {
        System.setProperty(
          "java.util.logging.config.file",
          ((Test / resourceDirectory).value / "logging.properties").absolutePath
        )
      }
    )

lazy val mongodb_record =
  persistenceProject("mongodb-record")
    .dependsOn(record, mongodb)
    .settings(
      crossScalaVersions := crossUpTo213,
      Test / parallelExecution := false
    )
