package olon
package http
package provider

import olon.common._
import olon.util._

import java.util.Locale
import java.util.ResourceBundle
import scala.annotation.nowarn

import Helpers._

/** Implement this trait in order to integrate Lift with other underlaying web
  * containers. Not necessarily JEE containers.
  */
trait HTTPProvider {

  // SCALA3 Using `uninitialized` instead of `_` (reverted for scala 2.13)
  @nowarn private var actualServlet: LiftServlet = _

  def liftServlet = actualServlet

  // Logger needs to be lazy to delay creation of logger until after boot. User can have changed the logging config
  private lazy val logger = Logger(classOf[HTTPProvider])

  protected def context: HTTPContext

  /** Call this from your implementation when the application terminates.
    */
  protected def terminate: Unit = {
    if (actualServlet != null) {
      actualServlet.destroy
      actualServlet = null
    }
  }

  /** Call this function in order for Lift to process this request
    * @param req
    *   \- the request object
    * @param resp
    *   \- the response object
    * @param chain
    *   \- function to be executed in case this request is supposed to not be
    *   processed by Lift
    */
  protected def service(req: HTTPRequest, resp: HTTPResponse)(
      chain: => Unit
  ) = {
    tryo {
      LiftRules.early.toList.foreach(_(req))
    }

    CurrentHTTPReqResp.doWith(req -> resp) {
      val newReq = Req(
        req,
        LiftRules.statelessRewrite.toList,
        Nil,
        LiftRules.statelessReqTest.toList,
        System.nanoTime
      )

      CurrentReq.doWith(newReq) {
        URLRewriter.doWith(url =>
          NamedPF
            .applyBox(resp.encodeUrl(url), LiftRules.urlDecorate.toList)
            .openOr(resp.encodeUrl(url))
        ) {
          if (
            !(isLiftRequest_?(newReq) &&
              actualServlet.service(newReq, resp))
          ) {
            chain
          }
        }
      }
    }
  }

  /** Executes Lift's Boot and makes necessary initializations
    */
  protected def bootLift(loader: Box[String]): Unit = {
    try {
      val b: Bootable = loader
        .map(b =>
          Class
            .forName(b)
            .getDeclaredConstructor()
            .newInstance()
            .asInstanceOf[Bootable]
        )
        .openOr(DefaultBootstrap)
      preBoot()
      b.boot()
    } catch {
      // The UnavailableException is the idiomatic way to tell a Java application container that
      // the boot process has gone horribly, horribly wrong. That _must_ bubble to the application
      // container that is invoking the app. See https://github.com/lift/framework/issues/1843
      case unavailableException: jakarta.servlet.UnavailableException =>
        logger.error(
          "Failed to Boot! An UnavailableException was thrown and all futher boot activities are aborted",
          unavailableException
        );

        throw unavailableException

      case e: Exception =>
        logger.error(
          "Failed to Boot! Your application may not run properly",
          e
        );
    } finally {
      postBoot()

      actualServlet = new LiftServlet(context)
      actualServlet.init
    }
  }

  private def preBoot(): Unit = {
    // do this stateless
    LiftRules.statelessDispatch.prepend(NamedPF("Classpath service") {
      case r @ Req(mainPath :: _, _, _)
          if (mainPath == LiftRules.resourceServerPath) =>
        ResourceServer.findResourceInClasspath(r, r.path.wholePath.drop(1))
    })
  }

  private def postBoot(): Unit = {
    if (!LiftRules.logServiceRequestTiming) {
      LiftRules.installServiceRequestTimer(NoOpServiceTimer)
    }
    try {
      ResourceBundle.getBundle(LiftRules.liftCoreResourceName)
    } catch {
      case _: Exception =>
        logger.error(
          "Olon core resource bundle for locale " + Locale
            .getDefault() + ", was not found ! "
        )
    } finally {
      LiftRules.bootFinished()
    }
  }

  private def liftHandled(in: String): Boolean =
    (in.indexOf(".") == -1) || in.endsWith(".html") || in.endsWith(".xhtml") ||
      in.endsWith(".htm") ||
      in.endsWith(".xml") || in.endsWith(".liftjs") || in.endsWith(".liftcss")

  /** Tests if a request should be handled by Lift or passed to the container to
    * be executed by other potential filters or servlets.
    */
  protected def isLiftRequest_?(session: Req): Boolean = {
    NamedPF.applyBox(session, LiftRules.liftRequest.toList) match {
      case Full(b) => b
      case _ =>
        session.path.endSlash ||
        (session.path.wholePath.takeRight(1) match {
          case Nil    => true
          case x :: _ => liftHandled(x)
        }) ||
        context.resource(session.uri) == null
    }
  }
}
