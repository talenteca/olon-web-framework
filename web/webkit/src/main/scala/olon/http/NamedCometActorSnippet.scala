package olon
package http

import scala.xml.NodeSeq

import common.Full

/** This trait adds a named comet actor on the page. *
  */
trait NamedCometActorSnippet {

  /** This is your Comet Class
    */
  def cometClass: String

  /** This is how you are naming your comet actors. It can be as simple as
    *
    * <pre name="code" class="scala"><code> def name= S.param(p).openOr("A")
    * </code></pre>
    *
    * This causes every comet actor for class @cometClass with the same @name to
    * include the same Comet Actor
    */
  def name: String

  /** The render method that inserts the <lift:comet> tag to add the comet actor
    * to the page
    */
  final def render(xhtml: NodeSeq): NodeSeq = {
    for (sess <- S.session)
      sess.sendCometMessage(
        cometClass,
        Full(name),
        CometName(name)
      )
    <lift:comet type={cometClass} name={name}>{xhtml}</lift:comet>
  }
}
