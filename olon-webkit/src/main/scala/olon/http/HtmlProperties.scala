package olon
package http

import olon.common._
import olon.util._

import java.io.InputStream
import java.io.Writer
import scala.xml.Elem
import scala.xml.Node
import scala.xml.NodeSeq

/** This trait encapsulates the various choices related to parsing and emitting
  * HTML/XHTML
  */
trait HtmlProperties {

  /** When we emit the HTML, what DocType will be emitted
    */
  def docType: Box[String]

  /** Creates a new instance of HtmlProperties with the docType property changed
    */
  def setDocType(newDocType: () => Box[String]) = {
    val old = this
    new HtmlProperties {
      def docType = newDocType()
      def encoding = old.encoding
      def contentType = old.contentType
      def htmlOutputHeader = old.htmlOutputHeader
      def htmlParser = old.htmlParser
      def htmlWriter = old.htmlWriter
      def html5FormsSupport = old.html5FormsSupport
      def maxOpenRequests = old.maxOpenRequests
      def userAgent = old.userAgent
    }
  }

  /** When we output the HTML, what encoding will be emitted
    */
  def encoding: Box[String]

  /** Creates a new instance of HtmlProperties with the encoding property
    * changed
    */
  def setEncoding(newEncoding: () => Box[String]) = {
    val old = this
    new HtmlProperties {
      def docType = old.docType
      def encoding = newEncoding()
      def contentType = old.contentType
      def htmlOutputHeader = old.htmlOutputHeader
      def htmlParser = old.htmlParser
      def htmlWriter = old.htmlWriter
      def html5FormsSupport = old.html5FormsSupport
      def maxOpenRequests = old.maxOpenRequests
      def userAgent = old.userAgent
    }
  }

  /** For XHTML, the Encoding appears before the DocType, except if you're
    * writing to IE6, so, rather than having a hard-coded calculation we allow
    * the calculation to be done here.
    */
  def htmlOutputHeader: Box[String]

  /** Creates a new instance of HtmlProperties with the htmlOutputHeader
    * property changed
    */
  def setHtmlOutputHeader(newHeader: () => Box[String]) = {
    val old = this
    new HtmlProperties {
      def docType = old.docType
      def encoding = old.encoding
      def htmlOutputHeader = newHeader()
      def contentType = old.contentType
      def htmlParser = old.htmlParser
      def htmlWriter = old.htmlWriter
      def html5FormsSupport = old.html5FormsSupport
      def maxOpenRequests = old.maxOpenRequests
      def userAgent = old.userAgent
    }
  }

  /** What's the content type that should be put in the response header?
    */
  def contentType: Box[String]

  /** Creates a new instance of HtmlProperties with the contentType property
    * changed
    */
  def setContentType(newContentType: () => Box[String]) = {
    val old = this
    new HtmlProperties {
      def docType = old.docType
      def encoding = old.encoding
      def contentType = newContentType()
      def htmlOutputHeader = old.htmlOutputHeader
      def htmlParser = old.htmlParser
      def htmlWriter = old.htmlWriter
      def html5FormsSupport = old.html5FormsSupport
      def maxOpenRequests = old.maxOpenRequests
      def userAgent = old.userAgent
    }
  }

  /** How are we parsing incoming files into a NodeSeq? This will likely point
    * to either PCDataXmlParser.apply or Html5.parse
    */
  def htmlParser: InputStream => Box[NodeSeq]

  /** Creates a new instance of HtmlProperties with the htmlParser property
    * changed
    */
  def setHtmlParser(newParser: InputStream => Box[NodeSeq]) = {
    val old = this
    new HtmlProperties {
      def docType = old.docType
      def encoding = old.encoding
      def contentType = old.contentType
      def htmlOutputHeader = old.htmlOutputHeader
      def htmlParser = newParser
      def htmlWriter = old.htmlWriter
      def html5FormsSupport = old.html5FormsSupport
      def maxOpenRequests = old.maxOpenRequests
      def userAgent = old.userAgent
    }
  }

  /** Given a NodeSeq and a Writer, convert the output to the writer.
    */
  def htmlWriter: (Node, Writer) => Unit

  /** Creates a new instance of HtmlProperties with the htmlWriter property
    * changed
    */
  def setHtmlWriter(newWriter: (Node, Writer) => Unit) = {
    val old = this
    new HtmlProperties {
      def docType = old.docType
      def encoding = old.encoding
      def contentType = old.contentType
      def htmlOutputHeader = old.htmlOutputHeader
      def htmlParser = old.htmlParser
      def htmlWriter = newWriter
      def html5FormsSupport = old.html5FormsSupport
      def maxOpenRequests = old.maxOpenRequests
      def userAgent = old.userAgent
    }
  }

  /** Are there HTML5 forms support?
    */
  def html5FormsSupport: Boolean

  /** Creates a new instance of HtmlProperties with the html5FormsSupport
    * property changed
    */
  def setHtml5FormsSupport(newFormsSupport: Boolean) = {
    val old = this
    new HtmlProperties {
      def docType = old.docType
      def encoding = old.encoding
      def contentType = old.contentType
      def htmlOutputHeader = old.htmlOutputHeader
      def htmlParser = old.htmlParser
      def htmlWriter = old.htmlWriter
      def html5FormsSupport = newFormsSupport
      def maxOpenRequests = old.maxOpenRequests
      def userAgent = old.userAgent
    }
  }

  /** What is the maximum number of open HTTP requests.
    */
  def maxOpenRequests: Int

  /** Creates a new instance of HtmlProperties with the maxOpenRequests property
    * changed
    */
  def setMaxOpenRequests(maxOpen: Int) = {
    val old = this
    new HtmlProperties {
      def docType = old.docType
      def encoding = old.encoding
      def contentType = old.contentType
      def htmlOutputHeader = old.htmlOutputHeader
      def htmlParser = old.htmlParser
      def htmlWriter = old.htmlWriter
      def html5FormsSupport = old.html5FormsSupport
      def maxOpenRequests = maxOpen
      def userAgent = old.userAgent
    }
  }

  /** What's the UserAgent that was used to create this HtmlChoice
    */
  def userAgent: Box[String]

  /** Creates a new instance of HtmlProperties with the userAgent property
    * changed
    */
  def setUserAgent(newUA: Box[String]) = {
    val old = this
    new HtmlProperties {
      def docType = old.docType
      def encoding = old.encoding
      def contentType = old.contentType
      def htmlOutputHeader = old.htmlOutputHeader
      def htmlParser = old.htmlParser
      def htmlWriter = old.htmlWriter
      def html5FormsSupport = old.html5FormsSupport
      def maxOpenRequests = old.maxOpenRequests
      def userAgent = newUA
    }
  }

}

/** This set of properties is based on Lift's current XHTML support
  */
final case class OldHtmlProperties(userAgent: Box[String])
    extends HtmlProperties {

  /** If you want to change the DocType header, override this method rather than
    * using setDocType.
    */
  def docType: Box[String] = {
    if (S.skipDocType) {
      Empty
    } else if (S.getDocType._1) {
      S.getDocType._2
    } else {
      Full(DocType.xhtmlTransitional)
    }
  }
  def encoding: Box[String] =
    Full(LiftRules.calculateXmlHeader(null, <ignore/>, contentType))

  def contentType: Box[String] = {
    val req = S.request
    val accept = req.flatMap(_.accepts)
    val key = req -> accept
    if (LiftRules.determineContentType.isDefinedAt(key)) {
      Full(LiftRules.determineContentType(key))
    } else {
      Empty
    }
  }

  def htmlParser: InputStream => Box[NodeSeq] = PCDataXmlParser.apply _

  def htmlWriter: (Node, Writer) => Unit =
    (n: Node, w: Writer) => {
      val sb = new StringBuilder(64000)
      AltXML.toXML(
        n,
        scala.xml.TopScope,
        sb,
        false,
        !LiftRules.convertToEntity.vend,
        S.legacyIeCompatibilityMode
      )
      w.append(sb)
      w.flush()
    }

  def htmlOutputHeader: Box[String] =
    (docType -> encoding) match {
      case (Full(dt), Full(enc)) if dt.length > 0 && enc.length > 0 =>
        if (LiftRules.calcIE6ForResponse() && LiftRules.flipDocTypeForIE6) {
          Full(dt.trim + "\n" + enc.trim + "\n")
        } else {
          Full(enc.trim + "\n" + dt.trim + "\n")
        }

      case (Full(dt), _) if dt.length > 0 => Full(dt.trim + "\n")

      case (_, Full(enc)) if enc.length > 0 => Full(enc.trim + "\n")

      case _ => Empty
    }

  val html5FormsSupport: Boolean = {
    val r = S.request openOr Req.nil
    r.isSafari5 || r.isFirefox36 || r.isFirefox40 ||
    r.isChrome5 || r.isChrome6
  }

  val maxOpenRequests: Int =
    LiftRules.maxConcurrentRequests.vend(S.request openOr Req.nil)
}

/** If you're going to use HTML5, then this is the set of properties to use
  */
final case class Html5Properties(userAgent: Box[String])
    extends HtmlProperties {
  def docType: Box[String] = Full("<!DOCTYPE html>")
  def encoding: Box[String] = Empty

  def contentType: Box[String] = {
    Full("text/html; charset=utf-8")
  }

  def htmlParser: InputStream => Box[Elem] = Html5.parse _

  def htmlWriter: (Node, Writer) => Unit =
    Html5.write(_, _, false, !LiftRules.convertToEntity.vend)

  def htmlOutputHeader: Box[String] = docType.map(_.trim + "\n")

  val html5FormsSupport: Boolean = {
    val r = S.request openOr Req.nil
    r.isSafari5 || r.isFirefox36 || r.isFirefox40 ||
    r.isChrome5 || r.isChrome6
  }

  val maxOpenRequests: Int =
    LiftRules.maxConcurrentRequests.vend(S.request openOr Req.nil)
}

/** If you're going to use HTML5 out, but want XHTML in (so you can have mixed
  * case snippet tags and you don't get the Html5 parsers obnoxious table
  * behavior), then this is the set of properties to use
  */
final case class XHtmlInHtml5OutProperties(userAgent: Box[String])
    extends HtmlProperties {
  def docType: Box[String] = Full("<!DOCTYPE html>")
  def encoding: Box[String] = Empty

  def contentType: Box[String] = {
    Full("text/html; charset=utf-8")
  }

  def htmlParser: InputStream => Box[NodeSeq] = PCDataXmlParser.apply _

  def htmlWriter: (Node, Writer) => Unit =
    Html5.write(_, _, false, !LiftRules.convertToEntity.vend)

  def htmlOutputHeader: Box[String] = docType.map(_ + "\n")

  val html5FormsSupport: Boolean = {
    val r = S.request openOr Req.nil
    r.isSafari5 || r.isFirefox36 || r.isFirefox40 ||
    r.isChrome5 || r.isChrome6
  }

  val maxOpenRequests: Int =
    LiftRules.maxConcurrentRequests.vend(S.request openOr Req.nil)
}
