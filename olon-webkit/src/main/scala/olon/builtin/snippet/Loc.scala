package olon
package builtin
package snippet

import olon.common._
import olon.http._

import scala.xml._

/** The Loc snippet is used to render localized content.
  *
  * Lookup resource with the id specified with the locid attribute. Will also
  * try to use the render method or the snippet body as locid if not specified.
  *
  * So these are equivalent:
  *
  * <lift:Loc locid="myid"/> <lift.Loc.myid/> <lift:Loc>myid</lift:Loc>
  *
  * There's a special case with the "i" method. It will use the text content as
  * the locid, and will replace the child node with the localized content
  * instead of the current element.
  *
  * This is especially useful together with designer friendly snippet markup:
  *
  * <h2 class="lift:Loc.i">Some header</h2>
  *
  * If the locid "Some header" for the current locale is e.g "En overskrift",
  * this will render
  *
  * <h2>En overskrift</h2>
  *
  * If the locid is not found, it will just render
  *
  * <h2>Some header</h2>
  */
object Loc extends DispatchSnippet {
  def dispatch: DispatchIt = {
    case "i" => ns => i(ns)
    case s   => ns => render(s, ns)
  }

  def i(ns: NodeSeq): NodeSeq = {
    ns match {
      case e: Elem => e.copy(child = S.loc(ns.text, Text(ns.text)))
      case _       => render("i", ns)
    }
  }

  def render(locId: String, kids: NodeSeq): NodeSeq = {
    S.loc(locId)
      .openOr(S.attr("locid") match {
        case Full(id) => S.loc(id, kids)
        case _        => S.loc(kids.text, kids)
      })
  }

}
