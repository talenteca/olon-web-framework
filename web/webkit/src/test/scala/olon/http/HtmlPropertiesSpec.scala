package olon
package http

import mockweb._

/** This only exists to keep the WebSpecSpec clean. Normally, you could just use
  * "() => bootstrap.Boot.boot".
  */
object HtmlPropertiesSpecBoot {
  def boot(): Unit = {
    LiftRules.htmlProperties.default.set((_: Req) match {
      case r @ Req("html5" :: _, _, _) =>
        println("Html5 request: " + r)
        Html5Properties(r.userAgent)
      case r =>
        println("other request: " + r)
        OldHtmlProperties(r.userAgent)
    })
  }
}

class HtmlPropertiesSpec extends WebSpec(HtmlPropertiesSpecBoot.boot _) {
  sequential

  "LiftRules.htmlProperties.default function" should {
    val testUrl1 = "http://example.com/html5/something"
    val testUrl2 = "http://example.com/anotherurl"

    val session1 = MockWeb.testS(testUrl1)(S.session)
    val session2 = MockWeb.testS(testUrl2)(S.session)

    "set S.htmlProperties to html5 when that is the first request" withSFor (testUrl1, session1) in {
      S.htmlProperties must haveClass[Html5Properties]
    }
    "set S.htmlProperties to xhtml when that is not the first request" withSFor (testUrl2, session1) in {
      S.htmlProperties must haveClass[OldHtmlProperties]
    }
    "set S.htmlProperties to xhtml when that is the first request" withSFor (testUrl2, session2) in {
      S.htmlProperties must haveClass[OldHtmlProperties]
    }
    "set S.htmlProperties to html5 when that is not the first request" withSFor (testUrl1, session2) in {
      S.htmlProperties must haveClass[Html5Properties]
    }
  }
}
