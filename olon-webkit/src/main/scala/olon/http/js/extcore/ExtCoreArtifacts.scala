package olon
package http
package js
package extcore

// SCALA3 Removing old import calls
//import olon.http.js.JsCmds

import scala.xml.NodeSeq

import JsCmds._
import util.Helpers._

/** JavaScript DSL to use the Ext Core JavaScript library
  */
object ExtCoreArtifacts extends JSArtifacts {

  /** Toggles between current JS object and the object denominated by id
    */
  def toggle(id: String) = new JsExp {
    def toJsCmd = "Ext.fly(" + id.encJs + ").toggle()"
  }

  /** Hides the element denominated by id
    */
  def hide(id: String) = new JsExp {
    def toJsCmd = "Ext.fly(" + id.encJs + ").hide()"
  }

  /** Shows the element denominated by this id
    */
  def show(id: String) = new JsExp {
    def toJsCmd = "Ext.fly(" + id.encJs + ").show()"
  }

  /** Shows the element denoinated by id and puts the focus on it
    */
  def showAndFocus(id: String) = new JsExp {
    def toJsCmd = "Ext.fly(" + id.encJs + ").show().focus(200)"
  }

  /** Serializes a form denominated by the id. It returns a query string
    * containing the fields that are to be submitted
    */
  def serialize(id: String) = new JsExp {
    def toJsCmd = "Ext.Ajax.serializeForm(" + id.encJs + ")"
  }

  /** Replaces the content of the node with the provided id with the markup
    * given by content
    */
  def replace(id: String, content: NodeSeq): JsCmd = new JsCmd with HtmlFixer {
    override val toJsCmd = {
      val (html, js) = fixHtmlAndJs("inline", content)

      val ret =
        """
	  try {
	  var parent1 = document.getElementById(""" + id.encJs + """);
	  parent1.innerHTML = """ + html + """;
	  for (var i = 0; i < parent1.childNodes.length; i++) {
	    var node = parent1.childNodes[i];
	    parent1.parentNode.insertBefore(node.cloneNode(true), parent1);
	  }
	  parent1.parentNode.removeChild(parent1);
	  } catch (e) {
	    // if the node doesn't exist or something else bad happens
	  }
	"""
      if (js.isEmpty) ret else ret + " " + js.toJsCmd

    }
  }

  /** Sets the inner HTML of the element denominated by the id
    */
  def setHtml(id: String, xml: NodeSeq): JsCmd = new JsCmd {
    def toJsCmd = fixHtmlCmdFunc(id, xml) { s =>
      "try { Ext.fly(" + id.encJs + ").dom.innerHTML = " + s + "; } catch (e) {}"
    }
  }

  /** Sets the JavScript that will be executed when document is ready for
    * processing
    */
  def onLoad(cmd: JsCmd): JsCmd = new JsCmd {
    def toJsCmd = "Ext.onReady(function() {" + cmd.toJsCmd + "})"
  }

  /** Fades out the element having the provided id, by waiting for the given
    * duration and fades out during fadeTime
    */
  def fadeOut(id: String, duration: TimeSpan, fadeTime: TimeSpan) = Noop

  /** Trabsforms a JSON object intoits string representation
    */
  def jsonStringify(in: JsExp): JsExp = new JsExp {
    def toJsCmd = "Ext.encode(" + in.toJsCmd + ")"
  }

  /** Converts a form denominated by formId into a JSON object
    */
  def formToJSON(formId: String): JsExp = new JsExp() {
    def toJsCmd = "Ext.urlDecode(Ext.Ajax.serializeForm(" + formId.encJs + "));"
  }

}
