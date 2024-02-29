package olon
package builtin
package snippet

import olon.common._
import olon.http._

import scala.collection.immutable.Map
import scala.xml._

object WithParamVar extends RequestVar[Map[String, NodeSeq]](Map.empty)

/** Evaluates the body and stores it in the WithParam RequestVar map. This map
  * is used in builtin.snippet.Surround to bind content to named sections. Note
  * that the WithParam snippet is also mapped to "bind-at"
  */
object WithParam extends DispatchSnippet {

  def dispatch: DispatchIt = { case _ =>
    // SCALA3 Removing `_` for passing function as a value
    render
  }

  /** Evaluates the body and stores it in the WithParam RequestVar map. This map
    * is used in builtin.snippet.Surround to bind content to named sections.
    * Note that the WithParam snippet is also mapped to "bind-at"
    */
  def render(kids: NodeSeq): NodeSeq = {
    (for {
      ctx <- S.session ?~ ("FIX" + "ME: Invalid session")
      _ <- S.request ?~ ("FIX" + "ME: Invalid request")
    } yield {
      val name: String = S.attr("name").openOr("main")
      val body = ctx.processSurroundAndInclude(PageName.get, kids)
      WithParamVar.atomicUpdate(_ + (name -> body))
      NodeSeq.Empty
    }) match {
      case Full(x) => x
      case Empty   => Comment("FIX" + "ME: session or request are invalid")
      case Failure(msg, _, _) => Comment(msg)
    }
  }

}
