package hello.snippet

import org.specs2._

class HelloSnippetTest extends mutable.Specification {

  "HelloWorldSnippet" should {
    "greet with hello" in {
      val helloSnippet = new HelloSnippet
      val str = helloSnippet.render(<span></span>).toString
      str.indexOf("Hello Olon") must be >= 0
    }
  }

}
