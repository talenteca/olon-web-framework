package olon
package builtin
package snippet

import olon.http._

import scala.xml._

object Ignore extends DispatchSnippet {

  def dispatch: DispatchIt = { case _ =>
    render _
  }

  def render(kids: NodeSeq): NodeSeq = NodeSeq.Empty
}
