package olon
package builtin
package snippet

import olon.common._
import olon.http._
import olon.util._

import scala.xml._

import Helpers._

object Comet extends DispatchSnippet with LazyLoggable {
  def dispatch: DispatchIt = { case _ =>
    // SCALA3 removing `_` for passing function as a value
    render
  }

  // Take the comet's internal container and annotate it with the unique
  // `containerId`.
  // SCALA3 Using `private` instead of `private[this]`
  private def buildContainer(
      cometHtml: NodeSeq,
      cometActor: LiftCometActor,
      containerId: String
  ): NodeSeq = {
    val cometVersion = http.S.requestCometVersions.is
      .collectFirst {
        case CometVersionPair(guid, ver) if guid == cometActor.uniqueId =>
          ver
      }
      .getOrElse("")

    cometActor.parentTag
      .copy(child = cometHtml) %
      ("id" -> containerId) %
      ("data-lift-comet-version" -> cometVersion)
  }

  /** Given a comet actor and the HTML contents for that actor, renders it into
    * a container and returns the results. Waits up to the timeout specified by
    * the comet actor's `cometRenderTimeout` property and then fails if the
    * actor has not yet properly rendered.
    */
  def containerForCometActor(
      cometActor: LiftCometActor,
      cometHtml: Box[NodeSeq] = Empty
  ): NodeSeq = {
    if (Props.devMode) {
      cometHtml.map { updatedHtml =>
        cometActor ! UpdateDefaultHtml(updatedHtml)
      }
    }

    cometActor !? (cometActor.cometRenderTimeout, AskRender) match {
      case Full(AnswerRender(response, _, _, _)) if cometActor.hasOuter =>
        buildContainer(
          cometActor.buildSpan(response.inSpan) ++ response.outSpan,
          cometActor,
          s"${cometActor.uniqueId}_outer"
        )

      case Full(AnswerRender(response, _, _, _)) =>
        cometActor.buildSpan(response.inSpan)

      case failedResult =>
        cometActor.cometRenderTimeoutHandler() openOr {
          throw new CometTimeoutException(
            s"Type: ${cometActor.theType}, name: ${cometActor.name}; result was: $failedResult"
          )
        }
    }
  }

  /** A typical comet tag could look like:
    *
    * {{{
    *   <div data-lift="comet?type="MyCometClass&name=optional-name-of-comet-instance">{xhtml}</div>
    * }}}
    *
    * For the name, you have three options
    *   - You can set a fixed name using `name=MyComet`
    *   - You can use a query parameter using `metaname`; e.g., for a url like
    *     `foo?id=122`, your comet could take the name "122" if you use:
    *     `metaname=id`
    *   - You could assign a random name by using `randomname=true`
    *
    * @param kids
    *   The NodeSeq that is enclosed by the comet tags
    */
  def render(cometHtml: NodeSeq): NodeSeq = {
    Props.inGAE match {
      case true => Text("Comet Disabled in Google App Engine")
      case _    => buildComet(cometHtml)
    }
  }

  private def buildComet(cometHtml: NodeSeq): NodeSeq = {
    val theType: Box[String] = S.attr.~("type").map(_.text)
    val cometName: Box[String] =
      S.currentAttr("name")
        .or(S.currentAttr("metaname").flatMap(S.param))
        .or(S.currentAttr("randomname").map(_ => Helpers.nextFuncName))

    try {
      theType match {
        case Full(cometType) =>
          S.findOrCreateComet(
            cometType,
            cometName,
            cometHtml,
            S.attrsFlattenToMap,
            true
          ).map { foundComet =>
            containerForCometActor(foundComet, Full(cometHtml))
          } match {
            case Full(cometContainer) => cometContainer

            case failedResult =>
              throw new CometNotFoundException(
                s"Type: ${cometType}, name: ${cometName}; result was: $failedResult"
              )
          }

        case _ =>
          throw NoCometTypeException
      }
    } catch {
      case _: StateInStatelessException =>
        throw new StateInStatelessException(
          "Lift does not support Comet for stateless requests"
        )
      case e: SnippetFailureException => throw e
      case e: Exception =>
        logger.error("Failed to find or render a comet actor", e)
        cometHtml
    }
  }
}

abstract class CometFailureException(msg: String)
    extends SnippetFailureException(msg) {
  override def buildStackTrace: NodeSeq = <div>{
    msg
  }</div> ++ super.buildStackTrace
}
object NoCometTypeException
    extends CometFailureException(
      "Comets with no type are no longer supported as of Lift 3."
    ) {
  def snippetFailure: LiftRules.SnippetFailures.Value =
    LiftRules.SnippetFailures.NoCometType
}

class CometTimeoutException(msg: String) extends CometFailureException(msg) {
  def snippetFailure: LiftRules.SnippetFailures.Value =
    LiftRules.SnippetFailures.CometTimeout
}

class CometNotFoundException(msg: String) extends CometFailureException(msg) {
  def snippetFailure: LiftRules.SnippetFailures.Value =
    LiftRules.SnippetFailures.CometNotFound
}
