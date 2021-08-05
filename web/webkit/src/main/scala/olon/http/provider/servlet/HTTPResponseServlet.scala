package olon
package http
package provider
package servlet

import scala.collection.mutable.{ListBuffer}
import java.io.{OutputStream}
import javax.servlet.http.{HttpServletResponse, Cookie}
import olon.http.provider.encoder.CookieEncoder
import olon.common._
import olon.util._
import Helpers._

class HTTPResponseServlet(resp: HttpServletResponse) extends HTTPResponse {
  private var _status = 0;

  private val SET_COOKIE_HEADER = "Set-Cookie"
 
  def addCookies(cookies: List[HTTPCookie]) = cookies.foreach {
    case cookie =>
      resp.addHeader(SET_COOKIE_HEADER, CookieEncoder.encode(cookie))
  }

  private val shouldEncodeUrl = LiftRules.encodeJSessionIdInUrl_?

  /**
   * Encode the JSESSIONID in the URL if specified by LiftRules
   */
  def encodeUrl(url: String): String = 
    if (shouldEncodeUrl) {
      resp encodeURL url
    } else {
      url
    }

  def addHeaders(headers: List[HTTPParam]) {
    val appearOnce = Set(LiftRules.overwrittenReponseHeaders.vend.map(_.toLowerCase): _*)
    for (h <- headers;
         value <- h.values) {
      if (appearOnce.contains(h.name.toLowerCase)) resp.setHeader(h.name, value)
      else
        resp.addHeader(h.name, value)
    }
  }

  def setStatus(status: Int) = {
    _status = status
    resp setStatus status
  }

  def getStatus = _status
 
  def setStatusWithReason(status: Int, reason: String) = {
    _status = status
    resp sendError  (status, reason)
  }

  def outputStream: OutputStream = resp.getOutputStream
}

