package olon
package http
package js
package jquery

import scala.xml.NodeSeq

import JqJE._
import JqJsCmds._
import util.Helpers._

trait JQueryArtifacts extends JSArtifacts {

  /** Toggles between current JS object and the object denominated by id
    */
  def toggle(id: String) = JqId(id) ~> new JsMember {
    def toJsCmd = "toggle()"
  }

  /** Hides the element denominated by id
    */
  def hide(id: String) = JqId(id) ~> new JsMember {
    def toJsCmd = "hide()"
  }

  /** Shows the element denominated by this id
    */
  def show(id: String) = JqId(id) ~> new JsMember {
    def toJsCmd = "show()"
  }

  /** Shows the element denominated by id and puts the focus on it
    */
  def showAndFocus(id: String) = JqId(id) ~> new JsMember {
    def toJsCmd =
      "show().each(function(i) {var t = this; setTimeout(function() { t.focus(); }, 200);})"
  }

  /** Serializes a form denominated by the id. It returns a query string
    * containing the fields that are to be submitted
    */
  def serialize(id: String) = JqId(id) ~> new JsMember {
    def toJsCmd = "serialize()"
  }

  /** Replaces the content of the node with the provided id with the markup
    * given by content
    */
  def replace(id: String, content: NodeSeq): JsCmd =
    JqJsCmds.JqReplace(id, content)

  /** Sets the inner HTML of the element denominated by the id
    */
  def setHtml(id: String, content: NodeSeq): JsCmd =
    JqJsCmds.JqSetHtml(id, content)

  /** Sets the JavScript that will be executed when document is ready for
    * processing
    */
  def onLoad(cmd: JsCmd): JsCmd = JqJsCmds.JqOnLoad(cmd)

  /** Fades out the element having the provided id, by waiting for the given
    * duration and fades out during fadeTime
    */
  def fadeOut(id: String, duration: TimeSpan, fadeTime: TimeSpan) =
    FadeOut(id, duration, fadeTime)

  /** Transforms a JSON object in to its string representation
    */
  def jsonStringify(in: JsExp): JsExp = new JsExp {
    def toJsCmd = "JSON.stringify(" + in.toJsCmd + ")"
  }

  /** Converts a form denominated by formId into a JSON object
    */
  def formToJSON(formId: String): JsExp = new JsExp() {
    def toJsCmd = "lift$.formToJSON('" + formId + "')";
  }

}

case object JQueryArtifacts extends JQueryArtifacts
