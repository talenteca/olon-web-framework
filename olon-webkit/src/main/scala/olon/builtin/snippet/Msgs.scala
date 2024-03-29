package olon
package builtin
package snippet

import olon.common.Box
import olon.common.Empty
import olon.http.js._
import olon.util.Helpers._

import scala.xml.Text
import scala.xml._

import http._
import common.Full

/** This built in snippet renders messages (Errors, Warnings, Notices) in a
  * <i>div</i>. Typically it is used in templates as a place holder for any
  * messages that are <b>not</b> associated with an ID. Setting the attribute
  * <i>showAll</i> to <i>true</i> will render all messages, with and without an
  * ID. This will lead to duplicate messages if additionally the <i>Msg</i>
  * built in snippet is used to show messages associated with an ID.
  *
  * E.g. (child nodes are optional) <pre name="code" class="xml"> &lt;lift:Msgs
  * showAll="false"&gt; &lt;lift:error_msg class="errorBox" &gt;Error! The
  * details are:&lt;/lift:error_msg&gt; &lt;lift:warning_msg&gt;Whoops, I had a
  * problem:&lt;/lift:warning_msg&gt;
  * &lt;lift:warning_class&gt;warningBox&lt;/lift:warning_class&gt;
  * &lt;lift:notice_msg&gt;Note:&lt;/lift:notice_msg&gt;
  * &lt;lift:notice_class&gt;noticeBox&lt;/lift:notice_class&gt;
  * &lt;/lift:snippet&gt; </pre>
  *
  * JavaScript fadeout and effects for the three types of notices (Errors,
  * Warnings and Notices) can be configured via LiftRules.noticesAutoFadeOut and
  * LiftRules.noticesEffects. Notices for individual elements based on id can be
  * rendered using the &lt;lift:msg/> tag.
  *
  * @see
  *   olon.builtin.snippet.Msg
  * @see
  *   olon.http.LiftRules#noticesAutoFadeOut
  * @see
  *   olon.http.LiftRules#noticesEffects
  */
object Msgs extends DispatchSnippet {
  // Dispatch to the render method no matter how we're called
  def dispatch: DispatchIt = { case _ =>
    render
  }

  /** This method performs extraction of custom formatting and then renders the
    * current notices.
    *
    * @see
    *   #renderNotices()
    */
  def render(styles: NodeSeq): NodeSeq = {
    // Capture the value for later AJAX updates
    ShowAll(toBoolean(S.attr("showAll") or S.attr("showall")))

    // Extract user-specified titles and CSS classes for later use
    List(
      (NoticeType.Error, MsgsErrorMeta),
      (NoticeType.Warning, MsgsWarningMeta),
      (NoticeType.Notice, MsgsNoticeMeta)
    ).foreach {
      case (noticeType, ajaxStorage) => {
        // Extract the title if provided, or default to none. Allow for XML nodes
        // so that people can localize, etc.
        val title: NodeSeq = (styles \\ noticeType.titleTag)
          .filter(_.prefix == "lift")
          .flatMap(_.child)

        // Extract any provided classes for the messages
        val cssClasses = ((styles \\ noticeType.styleTag) ++
          (styles \\ noticeType.titleTag \\ "@class")).toList
          .map(_.text.trim) match {
          case Nil     => Empty
          case classes => Full(classes.mkString(" "))
        }

        // Save the settings for AJAX usage
        ajaxStorage(Full(AjaxMessageMeta(title, cssClasses)))
      }
    }

    // Delegate the actual rendering to a shared method so that we don't
    // duplicate code for the AJAX pipeline
    (<div>{renderNotices()}</div> % ("id" -> LiftRules.noticesContainerId)) ++
      noticesFadeOut(NoticeType.Notice) ++
      noticesFadeOut(NoticeType.Warning) ++
      noticesFadeOut(NoticeType.Error) ++
      effects(NoticeType.Notice) ++
      effects(NoticeType.Warning) ++
      effects(NoticeType.Error)
  }

  /** This method renders the current notices to XHtml based on the current
    * user-specific formatting from the &lt;lift:Msgs> tag.
    */
  def renderNotices(): NodeSeq = {
    // Determine which formatting function to use based on tag usage
    val f =
      if (ShowAll.is) {
        S.messages _
      } else {
        S.noIdMessages _
      }

    // Compute the formatted set of messages for a given input
    def computeMessageDiv(
        args: (
            List[(NodeSeq, Box[String])],
            NoticeType.Value,
            SessionVar[Box[AjaxMessageMeta]]
        )
    ): NodeSeq = args match {
      case (messages, noticeType, ajaxStorage) =>
        // get current settings
        val title = ajaxStorage.get.map(_.title) openOr Text("")
        val styles = ajaxStorage.get.flatMap(_.cssClasses)

        // Compute the resulting div
        f(messages).toList.map(e => (<li>{e}</li>)) match {
          case Nil => Nil
          case msgList => {
            val ret = <div id={noticeType.id}>{title}<ul>{msgList}</ul></div>
            styles.foldLeft(ret)((xml, style) =>
              xml % new UnprefixedAttribute("class", Text(style), Null)
            )
          }
        }
    }

    // Render all three types together
    List(
      (S.errors, NoticeType.Error, MsgsErrorMeta),
      (S.warnings, NoticeType.Warning, MsgsWarningMeta),
      (S.notices, NoticeType.Notice, MsgsNoticeMeta)
    ).flatMap(computeMessageDiv)
  }

  /** This method wraps the JavaScript fade and effect scripts into lift's page
    * script that runs onLoad.
    */
  private[snippet] def appendScript(script: JsCmd): NodeSeq = {
    S.appendJs(script)
    NodeSeq.Empty
  }

  /** This method produces appropriate JavaScript to fade out the given notice
    * type. The caller must provide a default value for cases where fadeout is
    * not configured, as well as a wrapping function to transform the output.
    *
    * @see
    *   olon.http.LiftRules.noticesAutoFadeOut
    */
  def noticesFadeOut[T](
      noticeType: NoticeType.Value,
      default: T,
      wrap: JsCmd => T
  ): T =
    LiftRules.noticesAutoFadeOut()(noticeType) map {
      case (duration, fadeTime) => {
        wrap(LiftRules.jsArtifacts.fadeOut(noticeType.id, duration, fadeTime))
      }
    } openOr default

  /** This method produces and appends a script element to lift's page script to
    * fade out the given notice type.
    *
    * @see
    *   olon.http.LiftRules.noticesAutoFadeOut
    */
  def noticesFadeOut(noticeType: NoticeType.Value): NodeSeq =
    noticesFadeOut(noticeType, NodeSeq.Empty, appendScript)

  /** This method produces appropriate JavaScript to apply effects to the given
    * notice type. The caller must provide a default value for cases where
    * effects are not configured, as well as a wrapping function to transform
    * the output.
    *
    * @see
    *   olon.http.LiftRules.noticesEffects
    */
  def effects[T](
      noticeType: Box[NoticeType.Value],
      id: String,
      default: T,
      wrap: JsCmd => T
  ): T =
    LiftRules.noticesEffects()(noticeType, id) match {
      case Full(jsCmd) => wrap(jsCmd)
      case _           => default
    }

  /** This method produces and appends a script element to lift's page script to
    * apply effects to the given notice type.
    *
    * @see
    *   olon.http.LiftRules.noticesEffects
    */
  def effects(noticeType: NoticeType.Value): NodeSeq =
    effects(Full(noticeType), noticeType.id, NodeSeq.Empty, appendScript)
}

/** This SessionVar holds formatting data for notice notices so that the AJAX
  * and static notice renderers use the same formatting.
  */
object MsgsNoticeMeta extends SessionVar[Box[AjaxMessageMeta]](Empty) {
  override private[olon] def magicSessionVar_? = true
}

/** This SessionVar holds formatting data for warning notices so that the AJAX
  * and static notice renderers use the same formatting.
  */
object MsgsWarningMeta extends SessionVar[Box[AjaxMessageMeta]](Empty) {
  override private[olon] def magicSessionVar_? = true
}

/** This SessionVar holds formatting data for error notices so that the AJAX and
  * static notice renderers use the same formatting.
  */
object MsgsErrorMeta extends SessionVar[Box[AjaxMessageMeta]](Empty) {
  override private[olon] def magicSessionVar_? = true
}

/** This SessionVar records whether to show id-based messages in addition to
  * non-id messages.
  */
object ShowAll extends RequestVar[Boolean](false) {
  // override private[olon] def magicSessionVar_? = true
}

/** This case class is used to hold formatting data for the notice groups so
  * that AJAX and static notices render consistently.
  */
case class AjaxMessageMeta(title: NodeSeq, cssClasses: Box[String])
