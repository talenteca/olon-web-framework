package olon
package http
package provider

import olon.common._

import java.io.InputStream
import java.net.URL

/** Represents the service context information. Similar with servlet context.
  */
trait HTTPContext {

  /** @return
    *   the context path. It always comes first in a request URI. It is the URI
    *   part that represent to context of the request.
    */
  def path: String

  /** Returns the URL representation of a resource that is mapped by a fully
    * qualified path. The path is considered relative to the root path and it
    * always starts with '/'.
    *
    * @param path
    *   \- the resource path
    * @return
    *   \- the URL object of the path
    */
  def resource(path: String): URL

  /** Same as `[[resource]]` but returns an InputStream to read the resource.
    * @param path
    *   \- the resource path
    * @return
    *   InputStream
    */
  def resourceAsStream(path: String): InputStream

  /** @param path
    * @return
    *   the mime type mapped to resource determined by this path.
    */
  def mimeType(path: String): Box[String]

  /** @param name
    * @return
    *   the value of the init parameter identified by then provided name. Note
    *   that this is not typesfe and you need to explicitely do the casting when
    *   reading this attribute. Returns Empty if this parameter does not exist.
    */
  def initParam(name: String): Box[String]

  /** @return
    *   a List of Tuple2 consisting of name and value pair of the init
    *   parameters
    */
  def initParams: List[(String, String)]

  /** @param name
    * @return
    *   the value of the context attribute identified by then provided name.
    *   Returns Empty if this parameter does not exist.
    */
  def attribute(name: String): Box[Any]

  /** @return
    *   \- a List of Tuple2 consisting of name and value pair of the attributes
    */
  def attributes: List[(String, Any)]

  /** @param value.
    *   Any reference. Note that this is not typesfe and you need to explicitely
    *   cast when reading this attribute.
    */
  def setAttribute(name: String, value: Any): Unit

  /** @param name.
    *   The name ofthe parameter that needs to be removed.
    */
  def removeAttribute(name: String): Unit
}
