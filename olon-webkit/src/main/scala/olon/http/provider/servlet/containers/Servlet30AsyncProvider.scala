package olon
package http
package provider
package servlet
package containers

import olon.common._
import olon.http._
import olon.http.provider._
import olon.http.provider.servlet._

object Servlet30AsyncProvider extends AsyncProviderMeta {

  private lazy val (
    hasContinuations_?,
    _,
    _,
    startAsync,
    getResponse,
    complete,
    isSupported
  ) = {
    try {
      val cc = Class.forName("jakarta.servlet.ServletRequest")
      val asyncClass = Class.forName("jakarta.servlet.AsyncContext")
      val startAsync = cc.getMethod("startAsync")
      val getResponse = asyncClass.getMethod("getResponse")
      val complete = asyncClass.getMethod("complete")
      val isSupported = cc.getMethod("isAsyncSupported")
      (true, cc, asyncClass, startAsync, getResponse, complete, isSupported)
    } catch {
      case _: Exception =>
        (false, null, null, null, null, null, null)
    }
  }

  def suspendResumeSupport_? : Boolean = {
    hasContinuations_?
  }

  /** return a function that vends the ServletAsyncProvider
    */
  def providerFunction: Box[HTTPRequest => ServletAsyncProvider] =
    Full(req => new Servlet30AsyncProvider(req)).filter(_ =>
      suspendResumeSupport_?
    )

}

/** Servlet30AsyncProvider
  *
  * Implemented by using Servlet30 Continuation API
  */
class Servlet30AsyncProvider(req: HTTPRequest)
    extends ServletAsyncProvider
    with Loggable {
  import scala.language.reflectiveCalls

  private var asyncCtx: Object = null

  type SetTimeout = { def setTimeout(timeout: Long): Unit; }

  import Servlet30AsyncProvider._

  private lazy val servletReq = (req.asInstanceOf[HTTPRequestServlet]).req

  def suspendResumeSupport_? : Boolean = hasContinuations_? &&
    isSupported.invoke(servletReq).asInstanceOf[Boolean]

  def resumeInfo: Option[(Req, LiftResponse)] = None

  def suspend(timeout: Long): RetryState.Value = {
    asyncCtx = startAsync.invoke(servletReq)
    try {
      val st = asyncCtx.asInstanceOf[SetTimeout]
      st.setTimeout(0L)
    } catch {
      case e: Exception =>
        logger.error("Servlet 3.0 Async: Failed to set timeout", e)
    }
    logger.trace("Servlet 3.0 suspend")
    RetryState.SUSPENDED
  }

  def resume(what: (Req, LiftResponse)): Boolean = {
    logger.trace("Servlet 3.0 begin resume")
    val httpRes = getResponse
      .invoke(asyncCtx)
      .asInstanceOf[jakarta.servlet.http.HttpServletResponse]
    val httpResponse = new HTTPResponseServlet(httpRes)
    val liftServlet = req.provider.liftServlet
    try {
      liftServlet.sendResponse(what._2, httpResponse, what._1)
      complete.invoke(asyncCtx)
    } catch {
      case e: Exception =>
        logger.error("Servlet 3.0 Async: Couldn't resume thread", e)
    }
    logger.trace("Servlet 3.0 resume")
    true
  }
}
