package olon
package util

import common._

trait HasParams {
  def param(name: String): Box[String]
}

/** Impersonates a JSON command
  */
case class JsonCmd(
    command: String,
    target: String,
    params: Any,
    all: scala.collection.Map[String, Any]
)

import olon.json.JsonAST._

/** A helpful extractor to take the JValue sent from the client-side JSON stuff
  * and make some sense of it.
  */
object JsonCommand {

  implicit def iterableToOption[X](in: Iterable[X]): Option[X] =
    in.toSeq.headOption

  // SCALA3 adding JValue generic parameter type
  def unapply(in: JValue[?]): Option[(String, Option[String], JValue[?])] =
    for {
      // SCALA3 Adding case for patching pattern matching code sugaring
      case JString(command) <- in \ "command"
      params <- in \ "params"
      if params != JNothing
    } yield {
      val target = (in \ "target") match {
        case JString(t) => Some(t)
        case _          => None
      }
      (command, target, params)
    }
  // Some((in.command, in.target, in.params, in.all))
}

/** Holds information about a response
  */
class ResponseInfoHolder {
  var headers: Map[String, String] = Map.empty
  private var _docType: Box[String] = Empty
  private var _setDocType = false

  def docType = _docType

  def docType_=(in: Box[String]): Unit = {
    _docType = in
    _setDocType = true
  }

  def overrodeDocType = _setDocType
}
