package olon
package json

import org.scalacheck.Arbitrary
import org.scalacheck.Gen
import org.scalacheck.Prop._
import org.specs2.ScalaCheck
import org.specs2.mutable.Specification

import java.io.StringReader

/** System under specification for JSON Parser.
  */
class JsonParserSpec extends Specification with JValueGen with ScalaCheck {

  private def parseBadThing(): String = try {
    parse(
      """{"user":"AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA"<}"""
    )
    "x" * 1000
  } catch {
    case e: Throwable => e.getMessage
  }

  "JSON Parser Specification".title

  "Any valid json can be parsed" in {
    val parsing = (json: JValue) => { parse(prettyRender(json)); true }
    forAll(genJValue)(parsing)
  }

  "Parsing is thread safe" in {
    import java.util.concurrent._

    val json = Examples.person
    val executor = Executors.newFixedThreadPool(100)
    val results = (0 to 100)
      .map(_ =>
        executor.submit(new Callable[JValue] { def call: JValue = parse(json) })
      )
      .toList
      .map(_.get)
    results.zip(results.tail).forall(pair => pair._1 == pair._2) mustEqual true
  }

  "All valid string escape characters can be parsed" in {
    parse("[\"abc\\\"\\\\\\/\\b\\f\\n\\r\\t\\u00a0\"]") must_== JArray(
      JString("abc\"\\/\b\f\n\r\t\u00a0") :: Nil
    )
  }

  "Parser does not bleed prior results" in {
    parse(
      """{"a": "now is the time for all good men to come to the aid of their dog and eat dog food with other dogs and bark and woof and do dog things. now is the time for all good men to come to the aid of their dog and eat dog food with other dogs and bark and woof and do dog things. now is the time for all good men to come to the aid of their dog and eat dog food with other dogs and bark and woof and do dog things. now is the time for all good men to come to the aid of their dog and eat dog food with other dogs and bark and woof and do dog things. now is the time for all good men to come to the aid of their dog and eat dog food with other dogs and bark and woof and do dog things. now is the time for all good men to come to the aid of their dog and eat dog food with other dogs and bark and woof and do dog things. now is the time for all good men to come to the aid of their dog and eat dog food with other dogs and bark and woof and do dog things.now is the time for all good men to come to the aid of their dog and eat dog food with other dogs and bark and woof and do dog things"}"""
    )

    val msg = parseBadThing()

    msg.length must be_<=(50)
  }

  "Unclosed string literal fails parsing" in {
    parseOpt("{\"foo\":\"sd") mustEqual None
    parseOpt("{\"foo\":\"sd}") mustEqual None
  }

  "The EOF has reached when the Reader returns EOF" in {
    class StingyReader(s: String) extends java.io.StringReader(s) {
      override def read(cbuf: Array[Char], off: Int, len: Int): Int = {
        val c = read()
        if (c == -1) -1
        else {
          cbuf(off) = c.toChar
          1
        }
      }
    }

    val json = JsonParser.parse(new StingyReader(""" ["hello"] """))
    json mustEqual JArray(JString("hello") :: Nil)
  }

  "Segment size does not change parsing result" in {
    val bufSize = Gen.choose(2, 64)
    val parsing = (x: JValue, s1: Int, s2: Int) => {
      parseVal(x, s1) == parseVal(x, s2)
    }
    forAll(genObject, bufSize, bufSize)(parsing)
  }

  implicit def arbJValue: Arbitrary[JValue] = Arbitrary(genObject)

  private def parseVal(json: JValue, bufSize: Int) = {
    val segmentPool = new JsonParser.ArrayBlockingSegmentPool(bufSize)
    JsonParser.parse(
      new JsonParser.Buffer(
        new StringReader(compactRender(json)),
        false,
        segmentPool
      )
    )
  }
}
