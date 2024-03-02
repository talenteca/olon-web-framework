package olon
package builtin
package snippet

import olon.common.Empty
import olon.common.Full
import olon.common.Loggable
import olon.http.S._
import olon.http._
import olon.util.Helpers._

import scala.collection.mutable.HashMap
import scala.xml._

/** This class is a built in snippet that allows rendering only notices (Errors,
  * Warnings, Notices) that are associated with the id provided. This snippet
  * also renders effects configured for the given id. Typically this will be
  * used near by form fields to indicate that a certain field failed the
  * validation. For example:
  *
  * <pre name="code" class="xml"> &lt;input type="text" value=""
  * name="132746123548765"/&gt;&lt;lift:msg id="user_msg"/&gt; </pre>
  *
  * Additionally, you may specify additional CSS classes to be applied to each
  * type of notice using the followin attributes:
  *
  * <ul> <li>errorClass</li> <li>warningClass</li> <li>noticeClass</li> </ul>
  *
  * <pre name="code" class="xml"> &lt;input type="text" value=""
  * name="132746123548765"/&gt;&lt;lift:msg id="user_msg"
  * errorClass="error_class" warningClass="warning_class"
  * noticeClass="notice_class"/&gt; </pre>
  *
  * Notices for specific ids are set via the S.error(String,String) or
  * S.error(String,NodeSeq) methods. Global (non-id) notices are rendered via
  * the Msgs snippet.
  *
  * @see
  *   olon.builtin.snippet.Msgs
  * @see
  *   olon.http.S#error(String,String)
  * @see
  *   olon.http.S#error(String,NodeSeq)
  * @see
  *   olon.http.LiftRules#noticesEffects
  */
object Msg extends DispatchSnippet with Loggable {
  def dispatch: DispatchIt = { case _ =>
    render
  }

  /** This method performs extraction of the CSS class attributes as well as
    * rendering of any messages for the given id.
    */
  def render(styles: NodeSeq): NodeSeq = {
    logger.trace("Rendering styles " + styles.size + " nodes")
    attr("id") match {
      case Full(id) => {
        // Extract the currently set CSS
        (attr("errorClass")
          .or(attr("errorclass")))
          .map(cls => MsgErrorMeta += (id -> cls))
        (attr("warningClass")
          .or(attr("warningclass")))
          .map(cls => MsgWarningMeta += (id -> cls))
        (attr("noticeClass")
          .or(attr("noticeclass")))
          .map(cls => MsgNoticeMeta += (id -> cls))

        <span id={id}>{renderIdMsgs(id)}</span> ++ effects(id)
      }
      case _ => NodeSeq.Empty
    }
  }

  /** This method renders the &lt;span/> for a given id's notices, along with
    * any effects configured for the id.
    *
    * @see
    *   olon.http.S#error(String,String)
    * @see
    *   olon.http.S#error(String,NodeSeq)
    * @see
    *   olon.http.LiftRules#noticesEffects
    */
  def renderIdMsgs(id: String): NodeSeq = {
    val msgs: List[NodeSeq] = List(
      (S.messagesById(id)(S.errors), MsgErrorMeta.get.get(id)),
      (S.messagesById(id)(S.warnings), MsgWarningMeta.get.get(id)),
      (S.messagesById(id)(S.notices), MsgNoticeMeta.get.get(id))
    ).flatMap { case (msg, style) =>
      msg.toList match {
        case Nil => Nil
        case msgList =>
          style match {
            case Some(s) =>
              msgList.flatMap(t => <span>{t}</span> % ("class" -> s))
            case _ => msgList flatMap (n => n)
          }
      }
    }

    // Join multiple messages together with a comma
    msgs match {
      case Nil => Text("")
      case spans =>
        spans.reduceLeft { (output, span) =>
          output ++ Text(", ") ++ span
        }
    }
  }

  /** This method produces and appends a script element to lift's page script to
    * render effects on a element with the given id.
    *
    * @see
    *   olon.builtin.snippet.Msgs#effects[T](Box[NoticeType.Value],String,T,Box[JsCmd
    *   \=> T])
    */
  def effects(id: String): NodeSeq =
    Msgs.effects(Empty, id, NodeSeq.Empty, Msgs.appendScript)
}

/** This SessionVar holds a map of per-id CSS classes for error notices so that
  * the AJAX and static renderers use the same formatting.
  */
object MsgErrorMeta extends SessionVar[HashMap[String, String]](new HashMap) {
  override private[olon] def magicSessionVar_? = true
}

/** This SessionVar holds a map of per-id CSS classes for warning notices so
  * that the AJAX and static renderers use the same formatting.
  */
object MsgWarningMeta extends SessionVar[HashMap[String, String]](new HashMap) {
  override private[olon] def magicSessionVar_? = true
}

/** This SessionVar holds a map of per-id CSS classes for notice notices so that
  * the AJAX and static renderers use the same formatting.
  */
object MsgNoticeMeta extends SessionVar[HashMap[String, String]](new HashMap) {
  override private[olon] def magicSessionVar_? = true
}
