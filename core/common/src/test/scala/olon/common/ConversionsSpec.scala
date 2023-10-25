package olon
package common

import scala.xml.{NodeSeq, Text}

import org.specs2.matcher.XmlMatchers
import org.specs2.mutable.Specification


/**
 * System under specification for Conversions.
 */
class ConversionsSpec extends Specification with XmlMatchers {

  "A StringOrNodeSeq" should {

    "convert from a String" in {
      val sns: StringOrNodeSeq = "Hello"
      sns.nodeSeq must_== Text("Hello")
    }

    "convert from an Elem" in {
      val sns: StringOrNodeSeq = <b/>
      sns.nodeSeq must ==/ (<b/>)
    }

    "convert from a Seq[Node]" in {
      val sns: StringOrNodeSeq = List(<a/>, <b/>)
      sns.nodeSeq must ==/ (List(<a/>, <b/>) : NodeSeq)
    }
  }

  "A StringFunc" should {

    "be created by a String constant" in {
      val sf: StringFunc = "Foo"

      sf.func() must_== "Foo"
    }

    "be created by a String Function" in {
      val sf: StringFunc = () => "Bar"

      sf.func() must_== "Bar"
    }

    "be created by a constant that can be converted to a String" in {
      implicit def intToString(in: Int): String = in.toString
      val sf: StringFunc = 55

      sf.func() must_== "55"
    }

    "be created by a function that can be converted to a String" in {
      implicit def intToString(in: Int): String = in.toString
      val sf: StringFunc = () => 55

      sf.func() must_== "55"
    }

  }

  "A NodeSeq => NodeSeq function" should {

    "be created by a NodeSeq constant" in {
      val sf: NodeSeq => NodeSeq = _ => <b>Foo</b>
      sf(NodeSeq.Empty) must ==/ (<b>Foo</b>)
    }

    "be created by a NodeSeq Function" in {
      val sf: NodeSeq => NodeSeq = _ => <i>Bar</i>

      sf(NodeSeq.Empty) must ==/ (<i>Bar</i>)
    }

    "be created by a constant that can be converted to a NodeSeq" in {
      implicit def intToNS(in: Int): NodeSeq = <a>{in}</a>
      val sf: NodeSeq => NodeSeq = _ => 55
      sf(NodeSeq.Empty) must ==/ (<a>55</a>)
    }

    "be created by a function that can be converted to a NodeSeq" in {
      implicit def intToNodeSeq(in: Int): NodeSeq = <a>{in}</a>
      val sf: NodeSeq => NodeSeq = _ => 55
      sf(NodeSeq.Empty) must ==/ (<a>55</a>)
    }

  }

}

