package olon
package http

import org.specs2.matcher.XmlMatchers
import org.specs2.mutable.Specification

import scala.xml.Text

import common._
import util.Helpers._

/** System under specification for Bindings.
  */
class BindingsSpec extends Specification with XmlMatchers {
  "Bindings Bindings".title

  case class MyClass(str: String, i: Int, other: MyOtherClass)
  case class MyOtherClass(foo: String)
  /*
  trait MyClassBinding extends DataBinding[MyClass] {
    implicit val otherBinding: DataBinding[MyOtherClass]

    override def apply(entity: MyClass) = (xhtml: NodeSeq) => {
      val otherTemplate = chooseTemplate("myclass", "other", xhtml)
      bind(
        "myclass", xhtml,
        "str" -> Text("#" + entity.str + "#"),
        "i" -> Text(entity.i.toString),
        "other" -> entity.other.bind(otherTemplate)
      )
    }
  }


  object myOtherClassBinding extends DataBinding[MyOtherClass] {
    override def apply(other: MyOtherClass) = (xhtml: NodeSeq) => {
      bind("other", xhtml, "foo" -> Text("%" + other.foo + "%"))
    }
  }

  implicit object MyClassConcreteBinding extends MyClassBinding {
    override val otherBinding = myOtherClassBinding
  }
   */

  val template = <div>
    <span><myclass:str/></span>
    <span><myclass:i/></span>
    <myclass:other>
      <span><other:foo/></span>
    </myclass:other>
  </div>

  val expected = <div>
    <span>#hi#</span>
    <span>1</span>
    <span>%bar%</span>
  </div>
  /*
  "Bindings.binder with an available implicit databinding" should {
    "allow the application of that databinding to an appropriate object" in {
      MyClass("hi", 1, MyOtherClass("bar")).bind(template) must beEqualToIgnoringSpace(expected)
    }
  }
   */

  "SHtml" should {
    "deal with # in link" in {
      val session = new LiftSession("hello", "", Empty)

      val href = S.initIfUninitted(session) {
        val a = SHtml.link("/foo#bar", () => true, Text("Test"))

        (a \ "@href").text
      }

      href.endsWith("#bar") must_== true

    }
  }

  "CSS Selector Transforms" should {
    "retain attributes for input" in {
      val session = new LiftSession("hello", "", Empty)

      S.initIfUninitted(session) {
        val org = <span><input id="frog" class="dog cat"/></span>

        val res = ("#frog" #> SHtml.text("", _ => ())).apply(org)

        (res \ "input" \ "@id").text must_== "frog"

        (res \ "input" \ "@class").text must_== "dog cat"
      }
    }
  }
}
