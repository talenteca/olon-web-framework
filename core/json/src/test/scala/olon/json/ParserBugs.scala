package olon
package json

import util.control.Exception._

import org.specs2.mutable.Specification

object ParserBugs extends Specification {
  "Unicode ffff is a valid char in string literal" in {
    parseOpt(" {\"x\":\"\\uffff\"} ").isDefined mustEqual true
  }

  "Does not allow colon at start of array (1039)" in {
    parseOpt("""[:"foo", "bar"]""") mustEqual None
  }

  "Does not allow colon instead of comma in array (1039)" in {
    parseOpt("""["foo" : "bar"]""") mustEqual None
  }

  "Solo quote mark should fail cleanly (not StringIndexOutOfBoundsException) (1041)" in {
    JsonParser.parse("\"", discardParser) must throwA[JsonParser.ParseException].like {
      case e => e.getMessage must startWith("unexpected eof")
    }
  }

  "Field names must be quoted" in {
    val json = JObject(List(JField("foo\nbar", JInt(1))))
    val s = compactRender(json)
    (s mustEqual """{"foo\nbar":1}""") and
      (parse(s) mustEqual json)
  }

  "Double in scientific notation with + can be parsed" in {
    val json = JObject(List(JField("t", JDouble(12.3))))
    val s = """{"t" : 1.23e+1}"""
    parse(s) mustEqual json
  }

  private val discardParser = (p : JsonParser.Parser) => {
     var token: JsonParser.Token = null
     do {
       token = p.nextToken
     } while (token != JsonParser.End)
   }
}
