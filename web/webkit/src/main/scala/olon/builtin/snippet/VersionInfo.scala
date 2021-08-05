package olon
package builtin
package snippet

import scala.xml._
import olon.http._

object VersionInfo extends DispatchSnippet {

  def dispatch : DispatchIt = {
    case "lift" => liftVersion _
    case "date" => buildDate _
  }

 private def liftVersion(ignore: NodeSeq): NodeSeq =
 Text(LiftRules.liftVersion)

  private def buildDate(ignore: NodeSeq): NodeSeq =
  Text(LiftRules.liftBuildDate.toString)
}

