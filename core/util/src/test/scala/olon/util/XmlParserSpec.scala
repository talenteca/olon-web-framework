package olon
package util

import java.io.ByteArrayInputStream

import scala.xml.{Text, Unparsed}

import org.specs2.matcher.XmlMatchers
import org.specs2.mutable.Specification


/**
 * Systems under specification for XmlParser, specifically PCDataMarkupParser.
 */
class XmlParserSpec extends Specification with XmlMatchers {
  "Xml Parser Specification".title

  "Multiple attributes with same name, but different namespace" should {
    "parse correctly" >> {
      val actual =
      <lift:surround with="base" at="body">
        <lift:Menu.builder  li_path:class="p" li_item:class="i"/>
      </lift:surround>

      val expected =
      <lift:surround with="base" at="body">
        <lift:Menu.builder  li_path:class="p" li_item:class="i"/>
      </lift:surround>

      val bis = new ByteArrayInputStream(actual.toString.getBytes("UTF-8"))
      val parsed = PCDataXmlParser(bis).openOrThrowException("Test")
      parsed must ==/(expected)
    }

  }

  "XML can contain PCData" in {
    val data = <foo>{
        PCData("Hello Yak")
      }</foo>

    val str = AltXML.toXML(data, false, true)

    str.indexOf("<![CDATA[") must be > -1
  }

  "XML can contain Unparsed" in {
    val data = <foo>{
        Unparsed("Hello & goodbye > <yak Yak")
      }</foo>

    val str = AltXML.toXML(data, false, true)

    str.indexOf("Hello & goodbye > <yak Yak") must be > -1
  }

  "XML cannot contain Control characters" in {
     val data = 
     <foo>
      {
        '\u0085'
      }{
        Text("hello \u0000 \u0085 \u0080")
      }{
        "hello \u0000 \u0003 \u0085 \u0080"
      }{
        '\u0003'
      }
    </foo>

    val str = AltXML.toXML(data, false, true)

    def cntIllegal(in: Char): Int = in match {
      case '\u0085' => 1
      case c if (c >= '\u007f' && c <= '\u0095') => 1
      case '\n' => 0
      case '\r' => 0
      case '\t' => 0
      case c if c < ' ' => 1
      case _ => 0
    }

    str.toList.foldLeft(0)((a, b) => a + cntIllegal(b)) must_== 0
  }

}

