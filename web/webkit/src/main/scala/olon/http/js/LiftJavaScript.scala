package olon
package http
package js

import common._
import http.js._
import http.js.jquery.JQueryArtifacts
import JsCmds._
import JE._

// Script file for the current page.
private[http] object pageScript
    extends RequestVar[Box[JavaScriptResponse]](Empty)

/** Create a javascript command that will initialize lift.js using LiftRules.
  */
object LiftJavaScript {

  object PageJs {
    def unapply(req: Req): Option[JavaScriptResponse] = {
      val suffixedPath = req.path.wholePath
      val LiftPath = LiftRules.liftContextRelativePath
      val renderVersion = "([^.]+)\\.js".r

      suffixedPath match {
        case LiftPath :: "page" :: renderVersion(version) :: Nil =>
          RenderVersion.doWith(version) {
            pageScript.is.toOption
          }
        case other =>
          None
      }
    }
  }

  def servePageJs: LiftRules.DispatchPF = { case PageJs(response) =>
    () => Full(response)
  }

  def settings: JsObj = {
    val jsCometServer = LiftRules.cometServer().map(Str(_)).getOrElse(JsNull)
    JsObj(
      "liftPath" -> LiftRules.liftPath,
      "ajaxRetryCount" -> Num(LiftRules.ajaxRetryCount.openOr(3)),
      "ajaxPostTimeout" -> LiftRules.ajaxPostTimeout,
      "gcPollingInterval" -> LiftRules.liftGCPollingInterval,
      "gcFailureRetryTimeout" -> LiftRules.liftGCFailureRetryTimeout,
      "cometGetTimeout" -> LiftRules.cometGetTimeout,
      "cometFailureRetryTimeout" -> LiftRules.cometFailureRetryTimeout,
      "cometServer" -> jsCometServer,
      "logError" -> LiftRules.jsLogFunc
        .map(fnc => AnonFunc("msg", fnc(JsVar("msg"))))
        .openOr(AnonFunc("msg", Noop)),
      "ajaxOnFailure" -> LiftRules.ajaxDefaultFailure
        .map(fnc => AnonFunc(fnc()))
        .openOr(AnonFunc(Noop)),
      "ajaxOnStart" -> LiftRules.ajaxStart
        .map(fnc => AnonFunc(fnc()))
        .openOr(AnonFunc(Noop)),
      "ajaxOnEnd" -> LiftRules.ajaxEnd
        .map(fnc => AnonFunc(fnc()))
        .openOr(AnonFunc(Noop))
    )
  }

  def initCmd(settings: JsObj): JsCmd = {
    val extendJsHelpersCmd = LiftRules.jsArtifacts match {
      case JQueryArtifacts =>
        Call(
          "window.lift.extend",
          JsVar("lift_settings"),
          JsVar("window", "liftJQuery")
        )
      case _ =>
        Call(
          "window.lift.extend",
          JsVar("lift_settings"),
          JsVar("window", "liftVanilla")
        )
    }

    JsCrVar("lift_settings", JsObj()) &
      extendJsHelpersCmd &
      Call("window.lift.extend", JsVar("lift_settings"), settings) &
      Call("window.lift.init", JsVar("lift_settings"))
  }
}
