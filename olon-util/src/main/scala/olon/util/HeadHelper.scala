package olon
package util

import scala.xml._

import Helpers._

/** This object provides functions to setup the head section of html
  * documents.</p>
  */
object HeadHelper {

  /** This method returns its parameter unmodified.
    */
  def identity(xml: NodeSeq): NodeSeq = xml

  /** Removes duplicate node but treats <stript> and <link> tags differently.
    * <script> containing the same src attribute and <link> containing the same
    * href attribute value are considered duplicates.
    */
  def removeHtmlDuplicates(in: NodeSeq): NodeSeq = {
    var jsSources: Set[String] = Set()
    var hrefs: Set[String] = Set()

    Text("\n\t") ++ (in flatMap { e =>
      val src = e.attributes("src") match {
        case null => null
        case x    => x.text
      }

      val href = e.attributes("href") match {
        case null => null
        case x    => x.text
      }

      e match {
        case e: Elem
            if (e.label == "script") && (src != null) && (jsSources contains src) =>
          NodeSeq.Empty
        case e: Elem
            if (e.label == "script") && (src != null) && (!(jsSources contains src)) =>
          jsSources += src; e

        case e: Elem
            if (e.label == "link") && (href != null) && (hrefs contains href) =>
          NodeSeq.Empty
        case e: Elem
            if (e.label == "link") && (href != null) && !(hrefs contains href) =>
          hrefs += href; e

        case e: Text if (e.text.trim.length == 0) => NodeSeq.Empty

        case e => e
      }
    }).flatMap(e => e ++ Text("\n\t"))
  }

  /** This method finds all &lt;head&gt; tags that are descendants of
    * &lt;body&gt; tags in the specified NodeSequence and merges the contents of
    * those tags into the &lt;head&gt; tag closest to the root of the XML tree.
    */
  def mergeToHtmlHead(xhtml: NodeSeq): NodeSeq = {

    val headInBody: NodeSeq =
      (for (
        body <- xhtml \ "body";
        head <- findElems(body)(_.label == "head")
      ) yield head.child).flatMap { e => e }

    if (headInBody.isEmpty) {
      xhtml
    } else {
      def xform(in: NodeSeq, inBody: Boolean): NodeSeq = in flatMap {
        case e: Elem if !inBody && e.label == "body" =>
          // SCALA3 Using `x*` instead of `x: _*`
          Elem(
            e.prefix,
            e.label,
            e.attributes,
            e.scope,
            e.minimizeEmpty,
            xform(e.child, true)*
          )

        case e: Elem if inBody && e.label == "head" => NodeSeq.Empty

        case e: Elem if e.label == "head" =>
          // SCALA3 Using `x*` instead of `x: _*`
          Elem(
            e.prefix,
            e.label,
            e.attributes,
            e.scope,
            e.minimizeEmpty,
            removeHtmlDuplicates(e.child ++ headInBody)*
          )

        case e: Elem =>
          // SCALA3 Using `x*` instead of `x: _*`
          Elem(
            e.prefix,
            e.label,
            e.attributes,
            e.scope,
            e.minimizeEmpty,
            xform(e.child, inBody)*
          )

        case g: Group =>
          xform(g.child, inBody)

        case x => x
      }

      xform(xhtml, false)
    }
  }
}
