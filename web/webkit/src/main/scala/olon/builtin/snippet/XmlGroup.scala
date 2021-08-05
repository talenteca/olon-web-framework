package olon
package builtin
package snippet

import scala.xml._
import olon.http._

object XmlGroup extends DispatchSnippet {

  def dispatch : DispatchIt = {
    case _ => render _
  }

  /**
   * Returns the child nodes:
   *
   * <pre name="code" class="xml">
   *   &lt;lift:XmlGroup>
   *     &lt;div class="lift:MySnippet">
   *     &lt;/div>
   *     &lt;div class="lift:MyOtherSnippet">
   *     &lt;/div>
   *  &lt;/lift>
   * </pre>
   */
  def render(kids: NodeSeq): NodeSeq = kids
}

