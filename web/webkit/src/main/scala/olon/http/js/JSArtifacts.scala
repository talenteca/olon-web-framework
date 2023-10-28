package olon
package http
package js

import olon.common.Box
import olon.common.Empty
import olon.common.Full
import olon.util.Helpers._

import scala.xml.NodeSeq

/** Abstracted JavaScript artifacts used by lift core.
  */
trait JSArtifacts {

  /** Toggles the visibility of the element denomiated by id
    */
  def toggle(id: String): JsExp

  /** Hides the element denominated by id
    */
  def hide(id: String): JsExp

  /** Shows the element denominated by id
    */
  def show(id: String): JsExp

  /** Shows the element denominated by id and puts the focus on it
    */
  def showAndFocus(id: String): JsExp

  /** Serializes a form denominated by id. It returns a query string containing
    * the fields that are to be submitted
    */
  def serialize(id: String): JsExp

  /** Replaces the content of the node denominated by id with the markup given
    * by content
    */
  def replace(id: String, content: NodeSeq): JsCmd

  /** Sets the inner HTML of the element denominated by id
    */
  def setHtml(id: String, content: NodeSeq): JsCmd

  /** Queues the JavaScript in cmd for execution when the document is ready for
    * processing
    */
  def onLoad(cmd: JsCmd): JsCmd

  /** Fades out the element denominated by id, by waiting for duration
    * milliseconds and fading out for fadeTime milliseconds
    */
  def fadeOut(id: String, duration: TimeSpan, fadeTime: TimeSpan): JsCmd

  /** Transforms a JSON object into its string representation
    */
  def jsonStringify(in: JsExp): JsExp

  /** Converts a form denominated by formId into a JSON object
    */
  def formToJSON(formId: String): JsExp

  /** Rewrites the incomming path with the actual script path
    */
  def pathRewriter: PartialFunction[List[String], List[String]] =
    new PartialFunction[List[String], List[String]] {

      def isDefinedAt(in: List[String]): Boolean = false

      def apply(in: List[String]): List[String] = Nil

    }
}

/** The companion module for AjaxInfo that provides different construction
  * schemes
  */
object AjaxInfo {
  def apply(data: JsExp, post: Boolean) =
    new AjaxInfo(
      data,
      if (post) "POST" else "GET",
      1000,
      false,
      "script",
      Empty,
      Empty
    )

  def apply(data: JsExp, dataType: String, post: Boolean) =
    new AjaxInfo(
      data,
      if (post) "POST" else "GET",
      1000,
      false,
      dataType,
      Empty,
      Empty
    )

  def apply(data: JsExp) =
    new AjaxInfo(data, "POST", 1000, false, "script", Empty, Empty)

  def apply(data: JsExp, dataType: String) =
    new AjaxInfo(data, "POST", 1000, false, dataType, Empty, Empty)

  def apply(
      data: JsExp,
      post: Boolean,
      timeout: Long,
      successFunc: String,
      failFunc: String
  ) =
    new AjaxInfo(
      data,
      if (post) "POST" else "GET",
      timeout,
      false,
      "script",
      Full(successFunc),
      Full(failFunc)
    )
}

/** Represents the meta data of an Ajax request.
  */
case class AjaxInfo(
    data: JsExp,
    action: String,
    timeout: Long,
    cache: Boolean,
    dataType: String,
    successFunc: Box[String],
    failFunc: Box[String]
)
