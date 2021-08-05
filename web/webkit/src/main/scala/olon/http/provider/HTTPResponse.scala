package olon
package http
package provider

import java.io.{OutputStream}

/**
 * Represents the HTTP response that will be send to the client
 */
trait HTTPResponse {

  /**
   * Add cookies to the response
   *
   * @param cookies - the list of response cookies
   */
  def addCookies(cookies: List[HTTPCookie])

  /**
   * Encodes the URL such that it adds the session ID if it is necessary to the URL.
   * It is implementation specific detail how/if to add the session informations. This will be used
   * for URL rewriting purposes.
   *
   * @param url - the URL that needs to be analysed
   */
  def encodeUrl(url: String): String

  /**
   * Add a list of header parameters to the response object
   *
   * @param headers - the list of headers
   */
  def addHeaders(headers: List[HTTPParam])

  /**
   * Sets the HTTP response status
   *
   * @param status - the HTTP status
   */
  def setStatus(status: Int)

  /**
   * Returns the HTTP response status that has been set with setStatus
   * */
  def getStatus: Int

  /**
   * Sets the HTTP response status
   *
   * @param status - the HTTP status
   * @param reason - the HTTP reason phrase
   */
  def setStatusWithReason(status: Int, reason: String)

  /**
   * @return - the OutputStream that can be used to send down o the client the response body.
   */
  def outputStream: OutputStream
}

