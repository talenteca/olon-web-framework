package olon
package json

import org.json4s.native.JsonMethods
import org.scalacheck.Arbitrary
import org.scalacheck.Prop.forAll
import org.specs2.ScalaCheck
import org.specs2.mutable.Specification

import scala.util.Random

/** System under specification for JSON Printing.
  */
class JsonPrintingSpec extends Specification with JValueGen with ScalaCheck {
  "JSON Printing Specification".title

  "rendering does not change semantics" in {
    val rendering = (json: JValue) =>
      parse(JsonAST.prettyRender(json)) == parse(JsonAST.compactRender(json))
    forAll(rendering)
  }

  "rendering special double values by default" should {
    "render a standard double as is" in {
      val double = Random.nextDouble()
      JsonAST.compactRender(JDouble(double)) must_== double.toString
    }

    "render positive infinity as null" in {
      JsonAST.compactRender(JDouble(Double.PositiveInfinity)) must_== "null"
    }

    "render negative infinity as null" in {
      JsonAST.compactRender(JDouble(Double.NegativeInfinity)) must_== "null"
    }

    "render NaN as null" in {
      JsonAST.compactRender(JDouble(Double.NaN)) must_== "null"
    }
  }

  "rendering special double values with as-is handling" should {
    def render(json: JValue) = {
      JsonAST.render(
        json,
        JsonAST.RenderSettings(
          0,
          doubleRenderer = JsonAST.RenderSpecialDoubleValuesAsIs
        )
      )
    }

    "render a standard double as is" in {
      val double = Random.nextDouble()
      render(JDouble(double)) must_== double.toString
    }

    "render positive infinity as null" in {
      render(JDouble(Double.PositiveInfinity)) must_== "Infinity"
    }

    "render negative infinity as null" in {
      render(JDouble(Double.NegativeInfinity)) must_== "-Infinity"
    }

    "render NaN as null" in {
      render(JDouble(Double.NaN)) must_== "NaN"
    }
  }

  "rendering special double values with special value exceptions enabled" should {
    def render(json: JValue) = {
      JsonAST.render(
        json,
        JsonAST.RenderSettings(
          0,
          doubleRenderer = JsonAST.FailToRenderSpecialDoubleValues
        )
      )
    }

    "render a standard double as is" in {
      val double = Random.nextDouble()
      render(JDouble(double)) must_== double.toString
    }

    "throw an exception when attempting to render positive infinity" in {
      render(JDouble(Double.PositiveInfinity)) must throwAn[
        IllegalArgumentException
      ]
    }

    "throw an exception when attempting to render negative infinity" in {
      render(JDouble(Double.NegativeInfinity)) must throwAn[
        IllegalArgumentException
      ]
    }

    "throw an exception when attempting to render NaN" in {
      render(JDouble(Double.NaN)) must throwAn[IllegalArgumentException]
    }
  }

  private def parse(json: String) = JsonMethods.parseOpt(json)

  implicit def arbDoc: Arbitrary[JValue] = Arbitrary(genJValue)
}
