package olon
package http
package provider

/** Companion module for creating new HTTPParam objects
  */
object HTTPParam {
  def apply(name: String, value: String) = new HTTPParam(name, List(value))
}

/** Represents a HTTP query parameter or a HTTP header parameter
  */
case class HTTPParam(name: String, values: List[String])
