package olon
package builtin
package snippet

import olon.common._
import olon.http._

import scala.xml._

object Surround extends DispatchSnippet {

  def dispatch: DispatchIt = { case _ =>
    render _
  }

  def render(kids: NodeSeq): NodeSeq =
    (for {
      ctx <- S.session ?~ ("FIX" + "ME: Invalid session")
    } yield {
      def eatDiv(in: NodeSeq): NodeSeq =
        if (S.attr("eat").isDefined) in.flatMap {
          case e: Elem => e.child
          case n       => n
        }
        else in

      WithParamVar.doWith(Map()) {
        lazy val mainParam = (
          S.attr("at") openOr "main",
          eatDiv(ctx.processSurroundAndInclude(PageName.get, kids))
        )
        lazy val paramsMap = {
          val q = mainParam // perform the side-effecting thing here
          WithParamVar.get + q // WithParamVar is the side effects of processing the template
        }
        ctx.findAndMerge(S.attr("with"), paramsMap)
      }
    }) match {
      case Full(x) => x
      case Empty   => Comment("FIX" + "ME: session or request are invalid")
      case Failure(msg, _, _) => Comment(msg)
    }
}
