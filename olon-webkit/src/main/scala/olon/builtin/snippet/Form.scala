package olon
package builtin
package snippet

import olon.common._
import olon.http._
import olon.util._

import scala.xml._

import Helpers._

/** This object is the default handler for the &lt;lift:form&gt; tag, which is
  * used to perform AJAX submission of form contents. If the "onsubmit"
  * attribute is set on this tag, then the contents there will be run prior to
  * the actual AJAX call. If a "postsubmit" attribute is present on the tag,
  * then its contents will be executed after successful submission of the form.
  */
object Form extends DispatchSnippet {

  def dispatch: DispatchIt = {
    case "render" => render _
    case "ajax"   => render _
    case "post"   => post _
  }

  /** Add the post method and postback (current URL) as action. If the multipart
    * attribute is specified, set the enctype as "multipart/form-data"
    */
  def post(kids: NodeSeq): NodeSeq = {
    // yeah it's ugly, but I'm not sure
    // we could do it reliably with pattern matching
    // dpp Oct 29, 2010
    val ret: Elem =
      if (
        kids.length == 1 &&
        kids(0).isInstanceOf[Elem] &&
        (kids(0).prefix eq null) &&
        kids(0).label == "form"
      ) {
        val e = kids(0).asInstanceOf[Elem]
        val meta =
          new UnprefixedAttribute(
            "method",
            "post",
            new UnprefixedAttribute(
              "action",
              S.uri,
              e.attributes.filter {
                case up: UnprefixedAttribute =>
                  up.key != "method" && up.key != "action"
                case _ => true
              }
            )
          )
        new Elem(null, "form", meta, e.scope, e.minimizeEmpty, e.child: _*)
      } else {
        <form method="post" action={S.uri}>{kids}</form>
      }

    S.attr("multipart") match {
      case Full(x) if Helpers.toBoolean(x) =>
        ret % ("enctype" -> "multipart/form-data")
      case _ => ret
    }
  }

  def render(kids: NodeSeq): NodeSeq = {
    // yeah it's ugly, but I'm not sure
    // we could do it reliably with pattern matching
    // dpp Oct 29, 2010
    if (
      kids.length == 1 &&
      kids(0).isInstanceOf[Elem] &&
      (kids(0).prefix eq null) &&
      kids(0).label == "form"
    ) {
      new Elem(null, "form", addAjaxForm, TopScope, true, kids(0).child: _*)
    } else {
      Elem(null, "form", addAjaxForm, TopScope, true, kids: _*)
    }
  }

  private def addAjaxForm: MetaData = {
    val id = Helpers.nextFuncName

    val attr = S.currentAttrsToMetaData(name =>
      name != "id" && name != "onsubmit" && name != "action" && name != "form"
    )

    val pre = S.attr.~("onsubmit").map(_.text + ";") getOrElse ""

    val post = S.attr.~("postsubmit").map("function() { " + _.text + "; }")

    val ajax: String = pre + SHtml
      .makeAjaxCall(LiftRules.jsArtifacts.serialize(id), AjaxContext.js(post))
      .toJsCmd + ";" + "return false;"

    new UnprefixedAttribute(
      "id",
      Text(id),
      new UnprefixedAttribute(
        "action",
        Text("javascript://"),
        new UnprefixedAttribute("onsubmit", Text(ajax), attr)
      )
    )
  }
}
