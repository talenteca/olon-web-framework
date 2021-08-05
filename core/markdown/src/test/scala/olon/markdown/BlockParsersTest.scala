package olon.markdown
import org.junit.runner.RunWith
import org.scalatestplus.junit.JUnitRunner
import org.scalatest.{Matchers,FlatSpec}
import scala.xml.{Group, NodeSeq}

/**
 * Tests the parsing on block level.
 */
@RunWith(classOf[JUnitRunner])
class BlockParsersTest extends FlatSpec with Matchers with BlockParsers{

    "The BlockParsers" should "parse optional empty lines" in {
        val p = optEmptyLines
        val el = new EmptyLine(" \n")
        apply(p, Nil)   should equal (Nil)
        apply(p, List(el)) should equal (List(el))
        apply(p, List(el, el)) should equal (List(el, el))
    }

    it should "accept empty documents" in {
        val p = markdown
        val el = new EmptyLine(" \n")
        apply(p, Nil)   should equal (Nil)
        apply(p, List(el)) should equal (Nil)
        apply(p, List(el, el)) should equal (Nil)
    }

    it should "detect line types" in {
        val p = line(classOf[CodeLine])
        apply(p, List(new CodeLine("    ", "code"))) should equal (new CodeLine("    ", "code"))
        an [IllegalArgumentException] should be thrownBy(apply(p, List(new OtherLine("foo"))))
    }

    it should "correctly override list items markup" in {
        object MyDecorator extends Decorator {
            override def decorateItemOpen(): String = "<foo>"
            override def decorateItemClose(): String = "</foo>"
        }
        object MyTransformer extends Transformer {
            override def deco(): Decorator = MyDecorator
        }
        MyTransformer.apply("* Content") should equal ("<ul>\n<foo>Content</foo></ul>\n")
    }
}
