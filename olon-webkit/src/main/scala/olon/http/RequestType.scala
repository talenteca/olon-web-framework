package olon
package http

import provider._

abstract class RequestType extends Serializable {
  def post_? : Boolean = false

  def get_? : Boolean = false

  def head_? : Boolean = false

  def put_? : Boolean = false

  def patch_? : Boolean = false

  def delete_? : Boolean = false

  def options_? : Boolean = false

  def method: String
}

case object GetRequest extends RequestType {
  override def get_? = true
  val method = "GET"
}
case object PostRequest extends RequestType {
  override def post_? = true
  val method = "POST"
}
case object HeadRequest extends RequestType {
  override def head_? = true
  val method = "HEAD"
}
case object PutRequest extends RequestType {
  override def put_? = true
  val method = "PUT"
}
case object PatchRequest extends RequestType {
  override def patch_? : Boolean = true
  val method: String = "PATCH"
}
case object DeleteRequest extends RequestType {
  override def delete_? = true
  val method = "DELETE"
}
case object OptionsRequest extends RequestType {
  override def options_? = true
  val method = "OPTIONS"
}
case class UnknownRequest(method: String) extends RequestType

object RequestType {
  def apply(req: HTTPRequest): RequestType = {
    req.method.toUpperCase match {
      case "GET"     => GetRequest
      case "POST"    => PostRequest
      case "HEAD"    => HeadRequest
      case "PUT"     => PutRequest
      case "PATCH"   => PatchRequest
      case "DELETE"  => DeleteRequest
      case "OPTIONS" => OptionsRequest
      case meth      => UnknownRequest(meth)
    }
  }
}
