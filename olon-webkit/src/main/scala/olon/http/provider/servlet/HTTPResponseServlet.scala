package olon
package http
package provider
package servlet

import jakarta.servlet.http.HttpServletResponse
import olon.http.provider.encoder.CookieEncoder

import java.io.OutputStream

class HTTPResponseServlet(resp: HttpServletResponse) extends HTTPResponse {
  private var _status = 0;

  private val SET_COOKIE_HEADER = "Set-Cookie"

  def addCookies(cookies: List[HTTPCookie]) = cookies.foreach { case cookie =>
    resp.addHeader(SET_COOKIE_HEADER, CookieEncoder.encode(cookie))
  }

  private val shouldEncodeUrl = LiftRules.encodeJSessionIdInUrl_?

  /** Encode the JSESSIONID in the URL if specified by LiftRules
    */
  def encodeUrl(url: String): String =
    if (shouldEncodeUrl) {
      resp.encodeURL(url)
    } else {
      url
    }

  def addHeaders(headers: List[HTTPParam]): Unit = {
    // SCALA3 using `x*` instead of `x: _*`
    val appearOnce = Set(
      LiftRules.overwrittenReponseHeaders.vend.map(_.toLowerCase)*
    )
    for (
      h <- headers;
      value <- h.values
    ) {
      if (appearOnce.contains(h.name.toLowerCase)) resp.setHeader(h.name, value)
      else
        resp.addHeader(h.name, value)
    }
  }

  def setStatus(status: Int) = {
    _status = status
    resp.setStatus(status)
  }

  def getStatus = _status

  def setStatusWithReason(status: Int, reason: String) = {
    _status = status
    resp.sendError(status, reason)
  }

  def outputStream: OutputStream = resp.getOutputStream
}
