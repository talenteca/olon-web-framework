package olon
package builtin
package snippet

import olon.http._

import scala.xml._

/** Use this builtin snippet to either show or hide some html based on the user
  * being logged in or not.
  */
object TestCond extends DispatchSnippet {
  def dispatch: DispatchIt = {
    // SCALA3 Removing `_` for passing function as a value
    case "loggedin" | "logged_in" | "LoggedIn" | "loggedIn" => loggedIn
    // SCALA3 Removing `_` for passing function as a value
    case "loggedout" | "logged_out" | "LoggedOut" | "loggedOut" => loggedOut
  }

  def loggedIn(xhtml: NodeSeq): NodeSeq =
    if (S.loggedIn_?) xhtml else NodeSeq.Empty

  def loggedOut(xhtml: NodeSeq): NodeSeq =
    if (S.loggedIn_?) NodeSeq.Empty else xhtml
}
