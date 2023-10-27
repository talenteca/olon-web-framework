import sbt._
import Keys._

object OlonSbtHelpers {
  def coreProject = olonProject("core") _
  def webProject = olonProject("web") _
 
  /** Project definition helper that simplifies creation of `ProjectReference`.
    *
    * It is a convenience method to create a Olon `ProjectReference` module by having the boilerplate for most common
    * activities tucked in.
    *
    * @param base     the base path location of project module.
    * @param prefix   the prefix of project module.
    * @param module   the name of the project module. Typically, a project id is of the form olon-`module`.
    */
  def olonProject(base: String, prefix: String = "olon-")(module: String): Project =
    olonProject(id = if (module.startsWith(prefix)) module else prefix + module,
                base = file(base) / module.stripPrefix(prefix))

  def olonProject(id: String, base: File): Project = {
    Project(id, base)
      .settings(scalacOptions ++= List("-feature", "-language:implicitConversions", "-deprecation"))
      .settings(
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

  private def findApiUrl(moduleInfo: ModuleID, scalaBinaryVersionString: String): Option[URL] = {
    if (moduleInfo.organization == "ch.qos.logback" && moduleInfo.name == "logback-classic") {
      Some(url("https://FIXME_MISSING/logback-classic"))
    } else if (moduleInfo.organization == "ch.qos.logback" && moduleInfo.name == "logback-core") {
      Some(url("https://FIXME_MISSING/logback-core"))
    } else if (moduleInfo.organization == "commons-codec" && moduleInfo.name == "commons-codec") {
      Some(url("https://FIXME_MISSING/commons-codec"))
    } else if (moduleInfo.organization == "commons-httpclient" && moduleInfo.name == "commons-httpclient") {
      Some(url("https://FIXME_MISSING/commons-httpclient"))
    } else if (moduleInfo.organization == "commons-io" && moduleInfo.name == "commons-io") {
      Some(url("https://FIXME_MISSING/commons-io"))
    } else if (moduleInfo.organization == "commons-logging" && moduleInfo.name == "commons-logging") {
      Some(url("https://FIXME_MISSING/commons-logging"))
    } else if (moduleInfo.organization == "nu.validator" && moduleInfo.name == "htmlparser") {
      Some(url("https://FIXME_MISSING/htmlparser"))
    } else if (moduleInfo.organization == "javax.servlet" && moduleInfo.name == "javax.servlet-api") {
      Some(url("https://FIXME_MISSING/javax.servlet-api"))
    } else if (moduleInfo.organization == "org.jline" && moduleInfo.name == "jline") {
      Some(url("https://FIXME_MISSING/jline"))
    } else if (moduleInfo.organization == "net.java.dev.jna" && moduleInfo.name == "jna") {
      Some(url("https://FIXME_MISSING/jna"))
    } else if (moduleInfo.organization == "org.joda" && moduleInfo.name == "joda-convert") {
      Some(url("https://www.javadoc.io/doc/org.joda/joda-convert/" + moduleInfo.revision + "/index.html"))
      Some(url("https://FIXME_MISSING/joda-convert"))
    } else if (moduleInfo.organization == "joda-time" && moduleInfo.name == "joda-time") {
      Some(url("https://www.javadoc.io/doc/joda-time/joda-time/" + moduleInfo.revision + "/index.html"))
    } else if (moduleInfo.organization == "log4j" && moduleInfo.name == "log4j") {
      Some(url("https://FIXME_MISSING/log4j"))
    } else if (moduleInfo.organization == "com.thoughtworks.paranamer" && moduleInfo.name == "paranamer") {
      Some(url("https://FIXME_MISSING/paranamer"))
    } else if (moduleInfo.organization == "org.portable-scala" && moduleInfo.name == "portable-scala-reflect_2.13") {
      Some(url("https://FIXME_MISSING/scala-reflect_2.13"))
    } else if (moduleInfo.organization == "org.scala-lang.modules" && moduleInfo.name == "scala-parallel-collections_2.13") {
      Some(url("https://FIXME_MISSING/parallel-collections_2.13"))
    } else if (moduleInfo.organization == "org.scala-lang.modules" && moduleInfo.name == "scala-parser-combinators_2.13") {
      Some(url("https://FIXME_MISSING/parser-combinators_2.13"))
    } else if (moduleInfo.organization == "org.scala-lang.modules" && moduleInfo.name == "scala-xml_2.13") {
      Some(url("https://scala.github.io/scala-xml/api/" + moduleInfo.revision))
    } else if (moduleInfo.organization == "org.scalaz" && moduleInfo.name == "scalaz-core_2.13") {
      Some(url("https://FIXME_MISSING/scalaz-core_2.13"))
    } else if (moduleInfo.organization == "org.specs2" && moduleInfo.name == "specs2-analysis_2.13") {
      Some(url("https://FIXME_MISSING/specs2-analysis_2.13"))
    } else if (moduleInfo.organization == "org.specs2" && moduleInfo.name == "specs2-common_2.13") {
      Some(url("https://FIXME_MISSING/specs2-common_2.13"))
    } else if (moduleInfo.organization == "org.specs2" && moduleInfo.name == "specs2-core_2.13") {
      Some(url("https://FIXME_MISSING/specs2-core_2.13"))
    } else if (moduleInfo.organization == "org.specs2" && moduleInfo.name == "specs2-fp_2.13") {
      Some(url("https://FIXME_MISSING/specs2-fp_2.13"))
    } else if (moduleInfo.organization == "org.specs2" && moduleInfo.name == "specs2-matcher_2.13") {
      Some(url("https://FIXME_MISSING/specs2-matcher_2.13"))
    } else if (moduleInfo.organization == "org.specs2" && moduleInfo.name == "specs2-matcher-extra_2.13") {
      Some(url("https://FIXME_MISSING/matcher-extra_2.13"))
    } else if (moduleInfo.organization == "org.specs2" && moduleInfo.name == "xml_2.13") {
      Some(url("https://FIXME_MISSING/xml_2.13"))
    } else if (moduleInfo.organization == "commons-fileupload" && moduleInfo.name == "commons-fileupload") {
      Some(url("https://FIXME_MISSING/commons-fileupload"))
    } else if (moduleInfo.organization == "org.mindrot" && moduleInfo.name == "jbcrypt") {
      Some(url("https://FIXME_MISSING/jbcrypt"))
    } else if (moduleInfo.organization == "org.mozilla" && moduleInfo.name == "rhino") {
      Some(url("https://FIXME_MISSING/rhino"))
    } else if (moduleInfo.organization == "org.portable-scala" && moduleInfo.name == "portable-scala-reflect" + scalaBinaryVersionString) {
      Some(url("https://FIXME_MISSING/portable-scala-reflect"))
    } else if (moduleInfo.organization == "org.scala-lang" && moduleInfo.name == "scala-compiler") {
      Some(url("https://FIXME_MISSING/scala-compiler"))
    } else if (moduleInfo.organization == "org.scala-lang" && moduleInfo.name == "scala-library") {
      Some(url("https://www.scala-lang.org/api/" + moduleInfo.revision))
    } else if (moduleInfo.organization == "org.scala-lang" && moduleInfo.name == "scala-reflect") {
      Some(url("https://FIXME_MISSING/scala-reflect"))
    } else if (moduleInfo.organization == "org.scala-lang" && moduleInfo.name == "scalap") {
      Some(url("https://FIXME_MISSING/scalap"))
    } else if (moduleInfo.organization == "org.scala-lang.modules" && moduleInfo.name == "scala-parallel-collections") {
      Some(url("https://www.javadoc.io/doc/org.scala-lang.modules/scala-collection-contrib/ + scalaBinaryVersionString" + moduleInfo.revision + "/scala/collection/index.html"))
    } else if (moduleInfo.organization == "org.scala-lang.modules" && moduleInfo.name == "scala-parser-combinators") {
      Some(url("https://FIXME_MISSING/scala-parser-combinators"))
    } else if (moduleInfo.organization == "org.scala-sbt" && moduleInfo.name == "test-interface") {
      Some(url("https://FIXME_MISSING/test-interface"))
    } else if (moduleInfo.organization == "org.scalaz" && moduleInfo.name == "scalaz-core" + scalaBinaryVersionString) {
      Some(url("https://FIXME_MISSING/scalaz-core"))
    } else if (moduleInfo.organization == "org.slf4j" && moduleInfo.name == "slf4j-api") {
      Some(url("https://www.javadoc.io/doc/org.slf4j/slf4j-api/" + moduleInfo.revision + "/index.html"))
    } else if (moduleInfo.organization == "org.specs2" && moduleInfo.name == "classycle") {
      Some(url("https://FIXME_MISSING/classycle"))
    } else if (moduleInfo.organization == "org.specs2" && moduleInfo.name == "specs2-analysis" + scalaBinaryVersionString) {
      Some(url("https://FIXME_MISSING/specs2-analysis"))
    } else if (moduleInfo.organization == "org.specs2" && moduleInfo.name == "specs2-common" + scalaBinaryVersionString) {
      Some(url("https://FIXME_MISSING/specs2-common"))
    } else if (moduleInfo.organization == "org.specs2" && moduleInfo.name == "specs2-core" + scalaBinaryVersionString) {
      Some(url("https://FIXME_MISSING/specs2-core"))
    } else if (moduleInfo.organization == "org.specs2" && moduleInfo.name == "specs2-fp" + scalaBinaryVersionString) {
      Some(url("https://FIXME_MISSING/specs2-fp"))
    } else if (moduleInfo.organization == "org.specs2" && moduleInfo.name == "specs2-matcher" + scalaBinaryVersionString) {
      Some(url("https://FIXME_MISSING/specs2-matcher"))
    } else if (moduleInfo.organization == "org.specs2" && moduleInfo.name == "specs2-matcher-extra" + scalaBinaryVersionString) {
      Some(url("https://FIXME_MISSING/specs2-matcher-extra"))
    } else if (moduleInfo.organization == "org.specs2" && moduleInfo.name == "xml" + scalaBinaryVersionString) {
      Some(url("https://FIXME_MISSING/xml"))
    } else if (moduleInfo.organization == "org.webjars.bower" && moduleInfo.name == "jasmine") {
      Some(url("https://FIXME_MISSING/jasmine"))
    } else if (moduleInfo.organization == "org.webjars.bower" && moduleInfo.name == "jasmine-ajax") {
      Some(url("https://FIXME_MISSING/jasmine-ajax"))
    } else if (moduleInfo.organization == "org.webjars.bower" && moduleInfo.name == "jasmine-core") {
      Some(url("https://FIXME_MISSING/jasmine-core"))
    } else if (moduleInfo.organization == "org.webjars.bower" && moduleInfo.name == "jquery") {
      Some(url("https://FIXME_MISSING/jquery"))
    } else if (moduleInfo.organization == "xerces" && moduleInfo.name == "xercesImpl") {
      Some(url("https://FIXME_MISSING/xercesImpl"))
    } else if (moduleInfo.organization == "xml-apis" && moduleInfo.name == "xml-apis") {
      Some(url("https://FIXME_MISSING/xml-apis"))
    } else {
      None
    }
  }
}