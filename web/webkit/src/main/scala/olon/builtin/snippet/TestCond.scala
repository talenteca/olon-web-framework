package olon
package builtin
package snippet

import scala.xml._
import olon.http._

/**
 * Use this builtin snippet to either show or hide some html
 * based on the user being logged in or not.
 */
object TestCond extends DispatchSnippet {
  def dispatch : DispatchIt = {
    case "loggedin"  | "logged_in"  | "LoggedIn"  | "loggedIn"  => loggedIn _
    case "loggedout" | "logged_out" | "LoggedOut" | "loggedOut" => loggedOut _
  }

  def loggedIn(xhtml: NodeSeq): NodeSeq =
  if (S.loggedIn_?) xhtml else NodeSeq.Empty

  def loggedOut(xhtml: NodeSeq): NodeSeq =
  if (S.loggedIn_?) NodeSeq.Empty else xhtml
}

