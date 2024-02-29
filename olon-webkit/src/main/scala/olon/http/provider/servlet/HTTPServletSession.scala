package olon
package http
package provider
package servlet

import jakarta.servlet.http._
import olon.common._

class HTTPServletSession(session: HttpSession) extends HTTPSession {
  // SCALA3 Using `private` instead of `private[this]`
  private val servletSessionIdentifier =
    LiftRules.servletSessionIdentifier

  def sessionId: String = session.getId

  def link(liftSession: LiftSession) = session.setAttribute(
    servletSessionIdentifier,
    SessionToServletBridge(liftSession.underlyingId)
  )

  def unlink(liftSession: LiftSession) =
    session.removeAttribute(servletSessionIdentifier)

  def maxInactiveInterval: Long = session.getMaxInactiveInterval

  def setMaxInactiveInterval(interval: Long) =
    session.setMaxInactiveInterval(interval.toInt)

  def lastAccessedTime: Long = session.getLastAccessedTime

  def setAttribute(name: String, value: Any) = session.setAttribute(name, value)

  def attribute(name: String): Any = session.getAttribute(name)

  def removeAttribute(name: String) = session.removeAttribute(name)

  def terminate = session.invalidate
}

/** Represents the "bridge" between HttpSession and LiftSession
  */
case class SessionToServletBridge(uniqueId: String)
    extends HttpSessionBindingListener
    with HttpSessionActivationListener {
  override def sessionDidActivate(se: HttpSessionEvent) = {
    SessionMaster
      .getSession(uniqueId, Empty)
      .foreach(ls => LiftSession.onSessionActivate.foreach(_(ls)))
  }

  override def sessionWillPassivate(se: HttpSessionEvent) = {
    SessionMaster
      .getSession(uniqueId, Empty)
      .foreach(ls => LiftSession.onSessionPassivate.foreach(_(ls)))
  }

  override def valueBound(event: HttpSessionBindingEvent): Unit = {}

  /** When the session is unbound the the HTTP session, stop us
    */
  override def valueUnbound(event: HttpSessionBindingEvent): Unit = {
    SessionMaster.sendMsg(RemoveSession(uniqueId))
  }

}
