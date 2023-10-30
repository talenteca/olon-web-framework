package olon
package builtin
package snippet

import olon.common.Loggable
import olon.http._

import scala.xml._

object Ignore extends DispatchSnippet with Loggable {

  def dispatch: DispatchIt = { case _ =>
    render _
  }

  def render(kids: NodeSeq): NodeSeq = {
    logger.trace("Ignoring " + kids.size + " kids")
    NodeSeq.Empty
  }

}
