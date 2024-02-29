package olon
package builtin
package snippet

import olon.http._

import scala.xml._

object XmlGroup extends DispatchSnippet {

  def dispatch: DispatchIt = { case _ =>
    // SCALA3 Removing `_` for passing function as a value
    render
  }

  /** Returns the child nodes:
    *
    * <pre name="code" class="xml"> &lt;lift:XmlGroup> &lt;div
    * class="lift:MySnippet"> &lt;/div> &lt;div class="lift:MyOtherSnippet">
    * &lt;/div> &lt;/lift> </pre>
    */
  def render(kids: NodeSeq): NodeSeq = kids
}
