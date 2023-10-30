package olon
package http

import olon.common._

import provider._

/** The base trait of Controllers that handle pre-view requests
  */
trait SimpleController {
  def request: Req

  def httpRequest: HTTPRequest

  def param(name: String): Box[String] = {
    request.params.get(name) match {
      case None => Empty
      case Some(nl) =>
        nl.take(1) match {
          case Nil => Empty
          case l   => Full(l.head)
        }
    }
  }

  def post_? : Boolean = request.post_?

  def get(name: String): Box[String] =
    httpRequest.session.attribute(name) match {
      case null      => Empty
      case n: String => Full(n)
      case _         => Empty
    }

  def set(name: String, value: String): Unit = {
    httpRequest.session.setAttribute(name, value)
  }

  def unset(name: String): Unit = {
    httpRequest.session.removeAttribute(name)
  }
}
