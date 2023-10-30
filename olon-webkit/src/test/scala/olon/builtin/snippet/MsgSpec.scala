package olon
package builtin.snippet

import org.specs2.matcher.XmlMatchers
import org.specs2.mutable.Specification

import scala.xml._

import common._
import http._
import util.Helpers.secureXML

/** System under specification for Msg.
  */
class MsgSpec extends Specification with XmlMatchers {
  "Msg Specification".title

  def withSession[T](f: => T): T =
    S.initIfUninitted(new LiftSession("test", "", Empty))(f)

  "The built-in Msg snippet" should {
    "Properly render static content for a given id" in {
      withSession {
        // Set some notices
        S.error("foo", "Error")
        S.warning("bar", "Warning")
        S.notice("foo", "Notice")

        // We reparse due to inconsistencies with UnparsedAttributes
        val result = S.withAttrs(
          new UnprefixedAttribute(
            "id",
            Text("foo"),
            new UnprefixedAttribute("noticeClass", Text("funky"), Null)
          )
        ) {
          secureXML.loadString(Msg.render(<div/>).toString)
        }

        result must ==/(
          <span id="foo">Error, <span class="funky">Notice</span></span>
        )
      }
    }

    "Properly render AJAX content for a given id" in {
      withSession {
        // Set some notices
        S.error("foo", "Error")
        S.warning("bar", "Warning")
        S.notice("foo", "Notice")

        // We reparse due to inconsistencies with UnparsedAttributes
        val result = S.withAttrs(
          new UnprefixedAttribute(
            "id",
            Text("foo"),
            new UnprefixedAttribute("noticeClass", Text("funky"), Null)
          )
        ) {
          Msg.render(<div/>).toString // render this first so attrs get captured
          LiftRules.noticesToJsCmd().toString.replace("\n", "")
        }

        result must_== """JsCmd(jQuery('#'+"foo").html("Error, <span class=\"funky\">Notice</span>");jQuery('#'+"bar").html("Warning");)"""
      }
    }
  }
}
