import sbt._
import Keys._

object Dependencies {

  type ModuleMap = String => ModuleID

  lazy val slf4jVersion = "1.7.36"

  lazy val commons_codec              = "commons-codec"              % "commons-codec"               % "1.11"
  lazy val commons_fileupload         = "commons-fileupload"         % "commons-fileupload"          % "1.3.3"
  lazy val commons_httpclient         = "commons-httpclient"         % "commons-httpclient"          % "3.1"
  lazy val jbcrypt                    = "org.mindrot"                % "jbcrypt"                     % "0.4"
  lazy val joda_time                  = "joda-time"                  % "joda-time"                   % "2.10"
  lazy val joda_convert               = "org.joda"                   % "joda-convert"                % "2.1"
  lazy val htmlparser                 = "nu.validator"               % "htmlparser"                  % "1.4.12"
  lazy val paranamer                  = "com.thoughtworks.paranamer" % "paranamer"                   % "2.8"
  lazy val scalap: ModuleMap          = "org.scala-lang"             % "scalap"                      % _
  lazy val scala_compiler: ModuleMap  = "org.scala-lang"             % "scala-compiler"              % _
  lazy val slf4j_api                  = "org.slf4j"                  % "slf4j-api"                   % slf4jVersion
  lazy val scala_parallel_collections = "org.scala-lang.modules"     %% "scala-parallel-collections" % "0.2.0"
  lazy val scala_parser               = "org.scala-lang.modules"     %% "scala-parser-combinators"   % "1.1.2"
  lazy val scala_xml                  = "org.scala-lang.modules"     %% "scala-xml"                  % "2.1.0"
  lazy val rhino                      = "org.mozilla"                % "rhino"                       % "1.7.10"
  lazy val xerces                     = "xerces"                     % "xercesImpl"                  % "2.11.0"

  lazy val logback         = "ch.qos.logback"    % "logback-classic"       % "1.2.8"  % Provided
  lazy val servlet_api     = "javax.servlet"     % "javax.servlet-api"     % "3.1.0"  % Provided
  lazy val jquery          = "org.webjars.bower" % "jquery"                % "1.11.3" % Provided
  lazy val jasmineCore     = "org.webjars.bower" % "jasmine-core"          % "2.4.1"  % Provided
  lazy val jasmineAjax     = "org.webjars.bower" % "jasmine-ajax"          % "3.2.0"  % Provided
  lazy val log4j           = "log4j"             % "log4j"                 % "1.2.17" % Provided

  lazy val jetty6             = "org.mortbay.jetty"        % "jetty"                    % "6.1.26"        % Test
  lazy val jwebunit           = "net.sourceforge.jwebunit" % "jwebunit-htmlunit-plugin" % "2.5"           % Test
  lazy val specs2             = "org.specs2"               %% "specs2-core"             % "4.15.0"        % Test
  lazy val scalacheck         = "org.specs2"               %% "specs2-scalacheck"       % specs2.revision % Test
  lazy val specs2Prov         = "org.specs2"               %% "specs2-core"             % specs2.revision % Provided
  lazy val specs2Matchers     = "org.specs2"               %% "specs2-matcher-extra"    % specs2.revision % Test
  lazy val specs2MatchersProv = "org.specs2"               %% "specs2-matcher-extra"    % specs2.revision % Provided
  lazy val specs2Mock         = "org.specs2"               %% "specs2-mock"             % specs2.revision % Test
  lazy val json4s             = "org.json4s"               %% "json4s-native"           % "4.0.6"         % Test

}
