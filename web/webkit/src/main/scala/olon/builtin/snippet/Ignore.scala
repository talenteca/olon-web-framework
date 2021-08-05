package olon
package builtin
package snippet

import scala.xml._
import olon.http._

object Ignore extends DispatchSnippet {

  def dispatch : DispatchIt = {
    case _ => render _
  }

  def render(kids: NodeSeq) : NodeSeq = NodeSeq.Empty
}

