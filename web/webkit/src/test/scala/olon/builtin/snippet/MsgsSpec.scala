package olon
package builtin.snippet

import org.specs2.matcher.XmlMatchers
import org.specs2.mutable.Specification

import common._
import http._
import util.Helpers.secureXML

/**
 * System under specification for Msgs.
 */
class MsgsSpec extends Specification with XmlMatchers {
  "Msgs Specification".title

  def withSession[T](f: => T) : T =
    S.initIfUninitted(new LiftSession("test", "", Empty))(f)

  "The built-in Msgs snippet" should {
    "Properly render static content" in {
      val result = withSession {
        // Set some notices
        S.error("Error")
        S.warning("Warning")
        S.notice("Notice")

        // We reparse due to inconsistencies with UnparsedAttributes
        secureXML.loadString(Msgs.render(
          <lift:warning_msg>Warning:</lift:warning_msg><lift:notice_class>funky</lift:notice_class>
        ).toString)
      }

      result must ==/(
        <div id="lift__noticesContainer__">
          <div id="lift__noticesContainer___error">
            <ul>
              <li>Error</li>
            </ul>
          </div>
          <div id="lift__noticesContainer___warning">Warning:
            <ul>
              <li>Warning</li>
            </ul>
          </div>
          <div class="funky" id="lift__noticesContainer___notice">
            <ul>
              <li>Notice</li>
            </ul>
          </div>
        </div>)
    }

    "Properly render AJAX content" in {
      // TODO : Figure out how to test this
      pending
    }
  }
}

