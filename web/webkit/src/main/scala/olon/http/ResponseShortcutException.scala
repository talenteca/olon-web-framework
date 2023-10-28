package olon
package http

import olon.common._
import olon.util._

/** This exception is used by LiftSession.destroySessionAndContinueInNewSession
  * to unwind the stack so that the session can be destroyed and a new session
  * can be created and have the balance of the continuation executed in the
  * context of the new session.
  */
class ContinueResponseException(val continue: () => Nothing)
    extends LiftFlowOfControlException("Continue in new session")

object ContinueResponseException {
  def unapply(in: Throwable): Option[ContinueResponseException] = in match {
    case null                           => None
    case cre: ContinueResponseException => Some(cre)
    case e: Exception                   => unapply(e.getCause)
    case _                              => None
  }

}

final case class ResponseShortcutException(
    _response: () => LiftResponse,
    redirectTo: Box[String],
    doNotices: Boolean
) extends LiftFlowOfControlException("Shortcut") {
  lazy val response = _response()

  def this(resp: => LiftResponse, doNot: Boolean) =
    this(() => resp, Empty, doNot)
  def this(resp: => LiftResponse) = this(() => resp, Empty, false)
}

object ResponseShortcutException {
  def shortcutResponse(responseIt: => LiftResponse) =
    new ResponseShortcutException(responseIt, true)

  def redirect(to: String): ResponseShortcutException =
    new ResponseShortcutException(
      () => RedirectResponse(to, S.responseCookies: _*),
      Full(to),
      true
    )

  def redirect(to: String, func: () => Unit): ResponseShortcutException =
    S.session match {
      case Full(liftSession) =>
        redirect(liftSession.attachRedirectFunc(to, Full(func)))
      case _ => redirect(to)
    }

  def seeOther(to: String): ResponseShortcutException =
    new ResponseShortcutException(
      () => SeeOtherResponse(to, S.responseCookies: _*),
      Full(to),
      true
    )

  def seeOther(to: String, func: () => Unit): ResponseShortcutException =
    S.session match {
      case Full(liftSession) =>
        seeOther(liftSession.attachRedirectFunc(to, Full(func)))
      case _ => seeOther(to)
    }
}
