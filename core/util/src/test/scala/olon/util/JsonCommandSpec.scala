package olon
package util

import org.specs2.mutable.Specification

import json._

/** Systems under specification for JsonCommand.
  */
class JsonCommandSpec extends Specification {
  "JsonCommand Specification".title

  private def parse(in: String): JValue = JsonParser.parse(in)

  "The JsonCommand object" should {
    "return None for non-commands" in {
      JsonCommand.unapply(
        parse("""{"foo": "bar", "baz": false, "params": "moose"} """)
      ) must_== None
    }

    "return None for non-params" in {
      JsonCommand.unapply(
        parse("""{"command": "frog", "foo": "bar", "baz": false} """)
      ) must_== None
    }

    "Parse even if target missing" in {
      JsonCommand.unapply(
        parse("""{"command": "frog", "foo": "bar", "params": 99} """)
      ) must_== Some(("frog", None, JInt(99)))
    }

    "Parse the whole thing" in {
      JsonCommand.unapply(
        parse(
          """{"command": "frog", "target": "spud", "foo": "bar", "params": 982, "baz": false} """
        )
      ) must_==
        Some(("frog", Some("spud"), JInt(982)))
    }
  }
}
