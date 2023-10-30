package olon
package webapptest
package snippet

class HelloWorld {
  def howdy = <span>Welcome to webtest1 at {new java.util.Date}</span>
}

import scala.xml._
import olon.http._

class Meow extends Function1[NodeSeq, NodeSeq] {
  def apply(in: NodeSeq): NodeSeq = <yak/>
}

class Meower {
  def render: Meow = new Meow
}

class Splunker {
  def render = SHtml.onSubmit(_ => ())
}
