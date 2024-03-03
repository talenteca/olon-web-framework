package olon
package http
package provider
package servlet

import jakarta.servlet._
import jakarta.servlet.http._
import olon.common._
import olon.util._

import scala.compiletime.uninitialized

trait ServletFilterProvider extends Filter with HTTPProvider {

  // SCALA3 Using `uninitialized` instead of `_`
  var ctx: HTTPContext = uninitialized

  // We need to capture the ServletContext on init
  override def init(config: FilterConfig): Unit = {
    ctx = new HTTPServletContext(config.getServletContext)

    LiftRules.setContext(ctx)

    bootLift(Box.legacyNullTest(config.getInitParameter("bootloader")))

  }

  // And throw it away on destruction
  override def destroy: Unit = {
    ctx = null
    terminate
  }

  def context: HTTPContext = ctx

  /** Wrap the loans around the incoming request
    */
  private def handleLoanWrappers[T](f: => T): T = {
    val wrappers = LiftRules.allAround.toList

    def handleLoan(lst: List[LoanWrapper]): T = lst match {
      case Nil     => f
      case x :: xs => x(handleLoan(xs))
    }

    handleLoan(wrappers)
  }

  /** Executes the Lift filter component.
    */
  def doFilter(
      req: ServletRequest,
      res: ServletResponse,
      chain: FilterChain
  ) = {
    if (LiftRules.ending) chain.doFilter(req, res)
    else {
      LiftRules.reqCnt.incrementAndGet()
      try {
        TransientRequestVarHandler(
          Empty,
          RequestVarHandler(
            Empty,
            (req, res) match {
              case (
                    httpReq: HttpServletRequest,
                    httpRes: HttpServletResponse
                  ) =>
                val httpRequest = new HTTPRequestServlet(httpReq, this)
                val httpResponse = new HTTPResponseServlet(httpRes)

                handleLoanWrappers(service(httpRequest, httpResponse) {
                  chain.doFilter(req, res)
                })
              case _ => chain.doFilter(req, res)
            }
          )
        )
      } finally { LiftRules.reqCnt.decrementAndGet() }
    }
  }
}
