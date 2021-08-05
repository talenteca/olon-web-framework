package olon
package http

import org.specs2.matcher.XmlMatchers
import org.specs2.mutable.Specification
import util._
import Helpers._
import olon.mockweb.MockWeb._

class SHtmlSpec extends Specification with XmlMatchers {
  "SHtmlSpec Specification".title

  val html1= <span><input id="number"></input></span>

  val inputField1= testS("/")( ("#number" #> SHtml.number(0, println(_), 0, 100)).apply(html1)  )
  val inputField2= testS("/")( ("#number" #> SHtml.number(0, println(_: Double), 0, 100, 0.1)).apply(html1)  )
  val inputField3= testS("/")( ("#number" #> SHtml.number(0, println(_: Double), 0, 100, 1)).apply(html1)  )

  "SHtml" should {
    "create a number input field" in {
      inputField1 must \("input", "type" -> "number")
    }
    "create a number input field with min='0'" in {
      inputField1 must \("input", "min" -> "0")
    }
    "create a number input field with max='100'" in {
      inputField1 must \("input", "max" -> "100")
    }
    "create a number input field with step='0.1'" in {
      inputField2 must \("input", "step" -> "0.1")
    }
    "create a number input field with step='1.0'" in {
      inputField3 must \("input", "step" -> "1.0")
    }
    "Use the implicit from tuple to SelectableOption" in {
      testS("/")( ("#number" #> SHtml.select(Seq("Yes" -> "Yes" , "No" -> "No"), Some("value"), s => println(s) , "class" -> "form-control")).apply(html1)  )
      //compiling is enough for this test
      1 must_== 1
    }
  }
}
