package olon
package webapptest

import olon.common.Full
import org.specs2.matcher.XmlMatchers
import org.specs2.mutable.Specification

import java.net.InetAddress
import java.net.URI

import util._
import http._
import testing._
import Helpers._
import snippet.Counter

object OneShot extends Specification with RequestKit with XmlMatchers {
  sequential

  private def reachableLocalAddress = {
    val l = InetAddress.getLocalHost
    tryo { l.isReachable(50) } match {
      case Full(true) => l.getHostAddress
      case _          => "127.0.0.1"
    }
  }

  private val host_ =
    System.getProperty("olon.webapptest.oneshot.host", reachableLocalAddress)
  private val port_ =
    System.getProperty("olon.webapptest.oneshot.port", "8181").toInt

  private lazy val baseUrl_ = URI
    .create("http://%s:%s".format(host_, port_))
    .parseServerAuthority()
    .toURL()

  private lazy val jetty = new JettyTestServer(Full(baseUrl_))

  def baseUrl = jetty.baseUrl.toString

  step(jetty.start())

  "ContainerVars" should {
    "have correct int default" in {
      val tmp = LiftRules.sessionCreator
      try {
        LiftRules.sessionCreator = LiftRules.sessionCreatorForMigratorySessions

        val bx =
          for {
            resp <- get("/cv_int")
            xml <- resp.xml
          } yield xml

        bx.openOrThrowException("legacy code") must ==/(<int>45</int>).when(
          jetty.running
        )
      } finally {
        LiftRules.sessionCreator = tmp
      }
    }

    "be settable as Int" in {
      val tmp = LiftRules.sessionCreator
      try {
        LiftRules.sessionCreator = LiftRules.sessionCreatorForMigratorySessions

        val bx =
          for {
            resp <- get("/cv_int/33")
            resp2 <- resp.get("/cv_int")
            xml <- resp2.xml
          } yield xml

        bx.openOrThrowException("legacy code") must ==/(<int>33</int>).when(
          jetty.running
        )
      } finally {
        LiftRules.sessionCreator = tmp
      }
    }

    "be session aware" in {
      val tmp = LiftRules.sessionCreator
      try {
        LiftRules.sessionCreator = LiftRules.sessionCreatorForMigratorySessions

        val bx =
          for {
            resp <- get("/cv_int/33")
            resp2 <- resp.get("/cv_int")
            xml <- resp2.xml
            resp3 <- get("/cv_int")
            xml2 <- resp3.xml
          } yield (xml, xml2)

        bx.openOrThrowException("legacy code")._1 must ==/(<int>33</int>).when(
          jetty.running
        )
        bx.openOrThrowException("legacy code")._2 must ==/(<int>45</int>).when(
          jetty.running
        )
      } finally {
        LiftRules.sessionCreator = tmp
      }
    }

    "support multiple vars" in {
      val tmp = LiftRules.sessionCreator
      try {
        LiftRules.sessionCreator = LiftRules.sessionCreatorForMigratorySessions

        val bx =
          for {
            resp <- get("/cv_int/33")
            resp2 <- resp.get("/cv_int")
            _ <- resp.get("/cv_str/meow")
            resp3 <- resp.get("/cv_str")
            xml <- resp2.xml
            xml2 <- resp3.xml
          } yield (xml, xml2)

        bx.openOrThrowException("legacy code")._1 must ==/(<int>33</int>).when(
          jetty.running
        )
        bx.openOrThrowException("legacy code")._2 must ==/(<str>meow</str>)
          .when(jetty.running)

      } finally {
        LiftRules.sessionCreator = tmp
      }
    }
  }

  "OneShot" should {
    "fire once for oneshot" in {
      Counter.x = 0

      for {
        resp <- get("/oneshot")
        xml <- resp.html5AsXml
        span <- (xml \\ "span").filter(x => (x \ "@id").text == "one")
        in <- (span \\ "input")
        name <- in \ "@name"
      } {
        resp.get("/oneshot?" + urlEncode(name.text) + "=3")
        resp.get("/oneshot?" + urlEncode(name.text) + "=3")
      }

      Counter.x must be_==(1).when(jetty.running)
    }

    "fire multiple times for normal" in {
      Counter.x = 0

      for {
        resp <- get("/oneshot")
        xml <- resp.html5AsXml
        span <- (xml \\ "span").filter(x => (x \ "@id").text == "two")
        in <- (span \\ "input")
        name <- in \ "@name"
      } {
        resp.get("/oneshot?" + urlEncode(name.text) + "=3")
        resp.get("/oneshot?" + urlEncode(name.text) + "=3")
      }

      Counter.x must be_>=(2).when(jetty.running)
    }
  }

  step {
    tryo {
      jetty.stop()
    }
  }

}
