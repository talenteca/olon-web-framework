package olon
package webapptest

import org.specs2.mutable.Specification

import java.net.InetAddress
import java.net.URI

import common.Full
import util._
import http._
import Helpers.tryo

/** System under specification for ToHeadUsages.
  */
object ToHeadUsages extends Specification {
  "ToHeadUsages Specification".title
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
    System.getProperty("olon.webapptest.toheadusages.port", "8282").toInt

  private lazy val baseUrl_ = URI
    .create("http://%s:%s".format(host_, port_))
    .parseServerAuthority()
    .toURL()

  private lazy val jetty = new JettyTestServer(Full(baseUrl_))

  step(jetty.start())

  "lift <head> merger" should {

    "merge <head> from html fragment" in {
      jetty.browse(
        "/htmlFragmentWithHead",
        html =>
          html.getElementByXPath("/html/head/script[@id='fromFrag']") must not(
            beNull when jetty.running
          )
      )
    }

    "merge <head> from html fragment does not include head element in body" in {
      jetty.browse(
        "/htmlFragmentWithHead",
        html =>
          html
            .getElementsByXPath("/html/body/script[@id='fromFrag']")
            .size must (be_==(0) when jetty.running)
      )
    }

    "merge <head> from snippet" in {
      jetty.browse(
        "/htmlSnippetWithHead",
        html =>
          html.getElementByXPath(
            "/html/head/script[@src='snippet.js']"
          ) must not(beNull when jetty.running)
      )
    }

    "not merge for bodyless html" in {
      jetty.browse(
        "/basicDiv",
        html => {
          html.getElementById("fruit") must not(beNull when jetty.running)
          html.getElementById("bat") must not(beNull when jetty.running)
        }
      )
    }

    "not merge for headless bodyless html" in {
      jetty.browse(
        "/h1",
        html => {
          html.getElementById("h1") must not(beNull when jetty.running)
        }
      )
    }

    /*
    "not merge for headless body html" in {
      jetty.browse(
        "/body_no_head", html => {
          // Note: The XPath expression "html/body/head/div" fails here with
          // HtmlUnit 2.5 since "head" is not recognized as a XHTML element
          // due to its incorrect position (under body instead of directly under html)
          html.getElementsByXPath("/html/body//div").size must be_==(1).when(jetty.running)
        }
      )
    }*/

    "not merge non-html" in {
      jetty.browse(
        "/non_html",
        html => {
          html.getElementById("frog") must not(beNull when jetty.running)
        }
      )
    }

  }

  "pages " should {
    "Templates should recognize entities" in {
      val ns = Templates(List("index")).openOrThrowException("legacy code")
      val str = AltXML.toXML(ns(0), false, false, false)

      val idx = str.indexOf("&mdash;")
      (idx >= 0) must beTrue.when(jetty.running)
    }

    "Templates should not recognize entities" in {
      val ns = Templates(List("index")).openOrThrowException("legacy code")
      val str = AltXML.toXML(ns(0), false, true, false)

      val idx = str.indexOf("&mdash;")
      (idx >= 0) must beFalse.when(jetty.running)
    }

    /*
    "round trip entities" in {
      JettyTestServer.browse(
        "/index",html => {
          val idx = html.getPageSource.indexOf("&mdash;")
          (idx >= 0) must_== true
        }
      )
    }*/

  }

  "deferred snippets" should {
    "render" in {
      jetty.browse(
        "/deferred",
        html => {
          html.getElementById("second") must not(beNull when jetty.running)
        }
      )
    }

    "not deferred not in actor" in {
      jetty.browse(
        "/deferred",
        html => {
          html.getElementByXPath(
            "/html/body/span[@id='whack1']/span[@id='actor_false']"
          ) must not(beNull when jetty.running)
        }
      )
    }

    "deferred in actor" in {
      jetty.browse(
        "/deferred",
        html => {
          html.getElementByXPath(
            "/html/body/span[@id='whack2']/span[@id='actor_true']"
          ) must not(beNull when jetty.running)
        }
      )
    }

    "Exclude from context rewriting" in {
      val first = http.Req.normalizeHtml(
        "/wombat",
        <span>
          <a href="/foo" id="foo">foo</a>
          <a href="/bar" id="bar">bar</a>
        </span>
      )

      def excludeBar(in: String): Boolean = in.startsWith("/bar")

      // SCALA3 Removing `_` for passing function as a value
      val second =
        LiftRules.excludePathFromContextPathRewriting.doWith(excludeBar) {
          Req.normalizeHtml(
            "/wombat",
            <span>
            <a href="/foo" id="foo">foo</a>
            <a href="/bar" id="bar">bar</a>
          </span>
          )
        }

      ((first \\ "a").filter(e =>
        (e \ "@id").text == "foo"
      ) \ "@href").text must be_==("/wombat/foo").when(jetty.running)
      ((first \\ "a").filter(e =>
        (e \ "@id").text == "bar"
      ) \ "@href").text must be_==("/wombat/bar").when(jetty.running)
      ((second \\ "a").filter(e =>
        (e \ "@id").text == "foo"
      ) \ "@href").text must be_==("/wombat/foo").when(jetty.running)
      ((second \\ "a").filter(e =>
        (e \ "@id").text == "bar"
      ) \ "@href").text must be_==("/bar").when(jetty.running)
    }
  }

  step {
    tryo {
      jetty.stop()
    }
  }

}
