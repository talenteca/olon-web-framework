package olon
package builtin
package snippet

import olon.common._
import olon.http._

import scala.xml._

/** Sets the DocType to html5
  */
object HTML5 extends DispatchSnippet {

  def dispatch: DispatchIt = { case _ =>
    // SCALA3 Removing `_` for passing function as a value
    render
  }

  def render(xhtml: NodeSeq): NodeSeq = {
    S.setDocType(Full(DocType.html5))
    xhtml
  }
}
