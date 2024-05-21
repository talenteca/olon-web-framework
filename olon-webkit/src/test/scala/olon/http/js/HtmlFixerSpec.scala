package olon
package http
package js

import org.specs2.mutable.Specification

import common._

class HtmlFixerSpec extends Specification {
  "HtmlFixer" should {
    val testFixer = new HtmlFixer {}
    val testSession = new LiftSession("/context-path", "underlying id", Empty)
    val testRules = new LiftRules()
    testRules.extractInlineJavaScript = true

    "never extract inline JS in fixHtmlFunc" in WithLiftContext(
      testRules,
      testSession
    ) {
      testFixer.fixHtmlFunc("test", <div onclick="clickMe();"></div>)(
        identity
      ) must_==
        """"<div onclick=\"clickMe();\"></div>""""
    }
  }
}
