package olon
package builtin
package snippet

import olon.http._

import scala.xml._

object SkipDocType extends DispatchSnippet {

  def dispatch: DispatchIt = { case _ =>
    // SCALA3 Removing `_` for passing function as a value
    render
  }

  /** Useful if you need to omit the DocType from the returned html (calling the
    * page from JavaScript, etc
    */
  def render(kids: NodeSeq): NodeSeq = {
    S.skipDocType = true
    kids
  }
}
