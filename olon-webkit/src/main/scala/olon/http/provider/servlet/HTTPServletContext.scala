package olon
package http
package provider
package servlet

import olon.common._
import olon.http.provider._
import olon.util._

import java.io.InputStream
import java.net.URL
import javax.servlet.ServletContext

import Helpers._

class HTTPServletContext(val ctx: ServletContext) extends HTTPContext {
  def path: String = ctx.getContextPath

  def resource(path: String): URL = ctx getResource path

  def resourceAsStream(path: String): InputStream = ctx getResourceAsStream path

  def mimeType(path: String) = Box !! ctx.getMimeType(path)

  def initParam(name: String): Box[String] = Box !! ctx.getInitParameter(name)

  def initParams: List[(String, String)] = enumToList[String](
    ctx.getInitParameterNames.asInstanceOf[java.util.Enumeration[String]]
  ).map(n => (n, initParam(n) openOr ""))

  def attribute(name: String): Box[Any] = Box !! ctx.getAttribute(name)

  def attributes: List[(String, Any)] = enumToList[String](
    ctx.getAttributeNames.asInstanceOf[java.util.Enumeration[String]]
  ).map(n => (n, attribute(n) openOr ""))

  def setAttribute(name: String, value: Any): Unit = {
    ctx.setAttribute(name, value)
  }

  def removeAttribute(name: String): Unit = {
    ctx.removeAttribute(name)
  }

}
