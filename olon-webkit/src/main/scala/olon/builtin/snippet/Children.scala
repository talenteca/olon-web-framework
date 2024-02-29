package olon
package builtin
package snippet

import olon.http._

import scala.xml._

object Children extends DispatchSnippet {

  def dispatch: DispatchIt = { case _ =>
    // SCALA3 removing `_` for passing function as a value
    render
  }

  /** Returns the child nodes:
    *
    * <pre name="code" class="xml"> &lt;lift:Children> &lt;div
    * class="lift:MySnippet"> &lt;/div> &lt;div class="lift:MyOtherSnippet">
    * &lt;/div> &lt;/lift> </pre>
    */
  def render(kids: NodeSeq): NodeSeq = kids
}
