package olon
package builtin
package snippet

import olon.common.Loggable
import olon.http._

import scala.xml._

object Ignore extends DispatchSnippet with Loggable {

  def dispatch: DispatchIt = { case _ =>
    // SCALA3 Removing `_` for passing function as a value
    render
  }

  def render(kids: NodeSeq): NodeSeq = {
    logger.trace("Ignoring " + kids.size + " kids")
    NodeSeq.Empty
  }

}
