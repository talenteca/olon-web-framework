package olon
package builtin
package snippet

import olon.common._
import olon.http._
import olon.util._

import scala.xml._

import Helpers._
import Box._

/** This object implements the logic for the &lt;lift:embed&gt; tag. It supports
  * retrieving a template based on the "what" attribute, and any
  * &lt;lift:bind-at&gt; tags contained in the embed tag will be used to replace
  * &lt;lift:bind&gt; tags within the embedded template.
  */
object Embed extends DispatchSnippet {
  // Extract a lift:bind-at Elem with a name attribute, yielding the
  // Elem and the value of the name attribute.
  private object BindAtWithName {
    def unapply(in: Elem): Option[(Elem, String)] = {
      if (in.prefix == "lift" && in.label == "bind-at") {
        in.attribute("name").map { nameNode =>
          (in, nameNode.text)
        }
      } else {
        None
      }
    }
  }

  private lazy val logger = Logger(this.getClass)

  def dispatch: DispatchIt = { case _ =>
    render _
  }

  def render(kids: NodeSeq): NodeSeq = {
    for {
      ctx <- S.session ?~ ("FIX" + "ME: session is invalid")
      what <-
        S.attr ~ ("what") ?~ ("FIX" + "ME The 'what' attribute not defined. In order to embed a template, the 'what' attribute must be specified")
      templateOpt <- ctx.findTemplate(
        what.text
      ) ?~ ("FIX" + "ME trying to embed a template named '" + what + "', but the template was not found. ")
    } yield {
      (what, Templates.checkForContentId(templateOpt))
    }
  } match {
    case Full((_, template)) => {
      val bindings: Seq[CssSel] = kids.collect {
        case BindAtWithName(element, name) =>
          s"#$name" #> element.child
      }

      val bindFn =
        if (bindings.length > 1)
          bindings.reduceLeft(_ & _)
        else if (bindings.length == 1)
          bindings(0)
        else
          PassThru

      bindFn(template)
    }
    case Failure(msg, Full(ex), _) =>
      logger.error("'embed' snippet failed with message: " + msg, ex)
      throw new SnippetExecutionException("Embed Snippet failed: " + msg)

    case Failure(msg, _, _) =>
      logger.error("'embed' snippet failed with message: " + msg)
      throw new SnippetExecutionException("Embed Snippet failed: " + msg)

    case _ =>
      logger.error(
        "'embed' snippet failed because it was invoked outside session context"
      )
      throw new SnippetExecutionException("session is invalid")
  }

}
