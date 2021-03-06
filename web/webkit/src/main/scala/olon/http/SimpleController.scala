package olon
package http

import scala.collection.immutable.TreeMap
import olon.common._
import olon.util._
import provider._

/**
 * The base trait of Controllers that handle pre-view requests
 */
trait SimpleController
 {
  def request: Req

  def httpRequest: HTTPRequest

  def param(name: String): Box[String] = {
    request.params.get(name) match {
      case None => Empty
      case Some(nl) => nl.take(1) match {
        case Nil => Empty
        case l => Full(l.head)
      }
    }
  }

  def post_? : Boolean = request.post_?

  def get(name: String): Box[String] =
    httpRequest.session.attribute(name) match {
      case null => Empty
      case n: String => Full(n)
      case _ => Empty
    }

  def set(name: String, value: String) {
    httpRequest.session.setAttribute(name, value)
  }

  def unset(name: String) {
    httpRequest.session.removeAttribute(name)
  }
}

