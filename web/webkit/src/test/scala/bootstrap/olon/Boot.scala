package bootstrap.olon

import olon.util._
import olon.http._
import olon.sitemap._
import olon.sitemap.Loc._
import Helpers._

/**
  * A class that's instantiated early and run.  It allows the application
  * to modify lift's environment
  */
class Boot {
  def boot: Unit = {
    // where to search snippet
    LiftRules.addToPackages("olon.webapptest")

    LiftRules.dispatch.append(ContainerVarTests)
  }
}

import rest._

case class Moose(str: String)

object ContainerVarTests extends RestHelper {
  object StrVar extends ContainerVar("Hello")
  object IntVar extends ContainerVar(45)
  // object CaseVar extends ContainerVar(Moose("dog"))

  serve {
    case "cv_int" :: Nil Get _ => <int>{IntVar.is}</int>
    case "cv_int" :: AsInt(i) :: _ Get _ => {
      IntVar.set(i)
      <int>{IntVar.is}</int>
    }
  }

  serve {
    case "cv_str" :: Nil Get _ => <str>{StrVar.is}</str>
    case "cv_str" :: str :: _ Get _ => {
      StrVar.set(str)
      <str>{StrVar.is}</str>
    }
  }
}
