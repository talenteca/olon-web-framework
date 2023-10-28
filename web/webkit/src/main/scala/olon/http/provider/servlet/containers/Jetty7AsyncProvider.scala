package olon
package http
package provider
package servlet
package containers

import olon.common._
import olon.http._
import olon.http.provider._
import olon.http.provider.servlet._
import olon.util._

object Jetty7AsyncProvider extends AsyncProviderMeta {
  // contSupport below gets inferred as a Class[?0] existential.
  import scala.language.existentials

  private val (
    hasContinuations_?,
    contSupport,
    getContinuation,
    getAttribute,
    setAttribute,
    suspendMeth,
    setTimeout,
    resumeMeth,
    isExpired,
    isResumed
  ) = {
    try {
      val cc =
        Class.forName("org.eclipse.jetty.continuation.ContinuationSupport")
      val meth =
        cc.getMethod("getContinuation", classOf[javax.servlet.ServletRequest])
      val cci = Class.forName("org.eclipse.jetty.continuation.Continuation")
      val getAttribute = cci.getMethod("getAttribute", classOf[String])
      val setAttribute =
        cci.getMethod("setAttribute", classOf[String], classOf[AnyRef])
      val suspend = cci.getMethod("suspend")
      val setTimeout = cci.getMethod("setTimeout", java.lang.Long.TYPE)
      val resume = cci.getMethod("resume")
      val isExpired = cci.getMethod("isExpired")
      val isResumed = cci.getMethod("isResumed")
      (
        true,
        (cc),
        (meth),
        (getAttribute),
        (setAttribute),
        (suspend),
        setTimeout,
        resume,
        isExpired,
        isResumed
      )
    } catch {
      case e: Exception =>
        (false, null, null, null, null, null, null, null, null, null)
    }
  }

  def suspendResumeSupport_? : Boolean = hasContinuations_?

  /** return a function that vends the ServletAsyncProvider
    */
  def providerFunction: Box[HTTPRequest => ServletAsyncProvider] =
    Full(req => new Jetty7AsyncProvider(req)).filter(i =>
      suspendResumeSupport_?
    )
}

/** Jetty7AsyncProvider
  *
  * Implemented by using Jetty 7 Continuation API
  */
class Jetty7AsyncProvider(req: HTTPRequest) extends ServletAsyncProvider {

  import Jetty7AsyncProvider._

  private val servletReq = (req.asInstanceOf[HTTPRequestServlet]).req

  def suspendResumeSupport_? : Boolean = hasContinuations_?

  def resumeInfo: Option[(Req, LiftResponse)] =
    if (!hasContinuations_?) None
    else if (Props.inGAE) None
    else {
      val cont = getContinuation.invoke(contSupport, servletReq)
      val ret = getAttribute.invoke(cont, "__liftCometState")
      try {
        setAttribute.invoke(cont, "__liftCometState", null)
        ret match {
          case (r: Req, lr: LiftResponse) => Some(r -> lr)
          case _                          => None
        }
      } catch {
        case e: Exception => None
      }
    }

  def suspend(timeout: Long): RetryState.Value = {
    val cont = getContinuation.invoke(contSupport, servletReq)

    val expired = isExpired.invoke(cont).asInstanceOf[Boolean]
    val resumed = isResumed.invoke(cont).asInstanceOf[Boolean]

    if (expired)
      RetryState.TIMED_OUT
    else if (resumed)
      RetryState.RESUMED
    else {
      setTimeout.invoke(cont, java.lang.Long.valueOf(timeout))
      suspendMeth.invoke(cont)
      RetryState.SUSPENDED
    }

  }

  def resume(what: (Req, LiftResponse)): Boolean = {
    val cont = getContinuation.invoke(contSupport, servletReq)
    try {
      setAttribute.invoke(cont, "__liftCometState", what)
      resumeMeth.invoke(cont)
      true
    } catch {
      case e: Exception =>
        setAttribute.invoke(cont, "__liftCometState", null)
        false
    }
  }
}
