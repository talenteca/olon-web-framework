package olon
package util

import org.specs2.mutable.Specification

/** Systems under specification for ConnectionIdentifier.
  */
class ConnectionIdentifierSpec extends Specification {
  "ConnectionIdentifier Specification".title

  "Connection identifier" should {

    "be set by property" in {
      DefaultConnectionIdentifier.jndiName must_== "from_props"
    }
  }
}
