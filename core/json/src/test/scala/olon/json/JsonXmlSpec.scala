package olon
package json

import org.specs2.mutable.Specification
import org.specs2.ScalaCheck
import org.scalacheck.Arbitrary
import org.scalacheck.Prop.forAll


/**
 * System under specification for JSON XML.
 */
class JsonXmlSpec extends Specification  with NodeGen with JValueGen with ScalaCheck {
  "JSON XML Specification".title

  import Xml._
  import scala.xml.Node

  "Valid XML can be converted to JSON and back (symmetric op)" in {
    val conversion = (xml: Node) => { toXml(toJson(xml)).head == xml }
    forAll(conversion)
  }

  "JSON can be converted to XML, and back to valid JSON (non symmetric op)" in {
    val conversion = (json: JValue) => { parse(compactRender(toJson(toXml(json)))); true }
    forAll(conversion)
  }

  implicit def arbXml: Arbitrary[Node] = Arbitrary(genXml)
  implicit def arbJValue: Arbitrary[JValue] = Arbitrary(genObject)
}
