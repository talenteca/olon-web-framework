package olon
package builtin
package snippet

import scala.xml._

import http._
import util._

object Tail extends DispatchSnippet {
  def dispatch: DispatchIt = { case _ =>
    // SCALA3 Removing `_` for passing function as a value
    render
  }

  def render(xhtml: NodeSeq): NodeSeq = <tail>{xhtml}</tail>
}

/** The 'head' snippet. Use this snippet to move a chunk of
  */
object Head extends DispatchSnippet {
  lazy val valid = Set("title", "base", "link", "meta", "style", "script")

  def dispatch: DispatchIt = { case _ =>
    // SCALA3 Removing `_` for passing function as a value
    render
  }

  def render(_xhtml: NodeSeq): NodeSeq = {
    def validHeadTagsOnly(in: NodeSeq): NodeSeq =
      in flatMap {
        case Group(ns) => validHeadTagsOnly(ns)
        case e: Elem if (null eq e.prefix) && valid.contains(e.label) => {
          // SCALA3 using `x*` instead of `x: _*`
          new Elem(
            e.prefix,
            e.label,
            e.attributes,
            e.scope,
            e.minimizeEmpty,
            validHeadTagsOnly(e.child)*
          )
        }
        case e: Elem if (null eq e.prefix) => NodeSeq.Empty
        case x                             => x
      }

    val xhtml = validHeadTagsOnly(_xhtml)

    <head>{
      if (
        (S.attr("withResourceId")
          .or(
            S
              .attr("withresourceid")
          )
          .filter(Helpers.toBoolean)
          .isDefined)
      ) {
        WithResourceId.render(xhtml)
      } else {
        xhtml
      }
    }</head>
  }
}
