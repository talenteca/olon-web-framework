package olon
package util

import scala.xml._

/** The Helpers object provides a lot of utility functions:<ul> <li>Time and
  * date <li>URL <li>Hash generation <li>Class instantiation <li>Control
  * abstractions <li>Basic types conversions <li>XML bindings </ul>
  */

object Helpers
    extends TimeHelpers
    with StringHelpers
    with ListHelpers
    with SecurityHelpers
    with HtmlHelpers
    with HttpHelpers
    with IoHelpers
    with BasicTypesHelpers
    with ClassHelpers
    with ControlHelpers

/** Used for type-safe pattern matching of an Any and returns a Seq[Node]
  */
object SafeNodeSeq {
  // I didn't use unapplySeq as I ran into a compiler(2.7.1 final) crash at LiftRules#convertResponse.
  // I opened the scala ticket https://lampsvn.epfl.ch/trac/scala/ticket/1059#comment:1
  def unapply(any: Any): Option[Seq[Node]] = any match {
    case s: Seq[_] =>
      Some(s flatMap (_ match {
        case n: Node => n
        case _       => NodeSeq.Empty
      }))
    case _ => None
  }
}

/** The superclass for all Lift flow of control exceptions
  */
class LiftFlowOfControlException(msg: String) extends RuntimeException(msg) {
  override def fillInStackTrace = this
}
