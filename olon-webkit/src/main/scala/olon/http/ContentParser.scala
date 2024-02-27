package olon
package http

import olon.common.Box
import olon.util.Helpers

import java.io.InputStream
import scala.xml.NodeSeq

/** Objects which can parse content should implement this trait. See
  * [[LiftRules.contentParsers]]
  */
trait ContentParser {

  /** The template filename suffixes this parser is for, e.g. "html", "md",
    * "adoc", etc.
    */
  def templateSuffixes: Seq[String]

  /** Given an InputStream to a resource with a matching suffix, this function
    * should provide the corresponding NodeSeq ready to serve
    * @return
    */
  def parse(content: InputStream): Box[NodeSeq]

  /** Called to auto-surround the content when needed, i.e. when this content is
    * a top-level template
    */
  def surround(content: NodeSeq): NodeSeq
}

object ContentParser {

  /** Convenience function to convert a simple `String => Box[NodeSeq]` content
    * parser function into a `InputStream => Box[NodeSeq]` function for
    * satisfying the `ContentParser` contract.
    * @param simpleParser
    *   your `String => Box[NodeSeq]` content parser
    * @return
    *   your parser wrapped up to handle an `InputStream`
    */
  def toInputStreamParser(
      simpleParser: String => Box[NodeSeq]
  ): InputStream => Box[NodeSeq] = { (is: InputStream) =>
    for {
      bytes <- Helpers.tryo(Helpers.readWholeStream(is))
      elems <- simpleParser(new String(bytes, "UTF-8"))
    } yield {
      elems
    }
  }

  /** Default surround function used by `ContentParser.basic` and the built-it
    * markdown parser which results in the template being surrounded by
    * `default.html` with the content located at `id=content`.
    */
  val defaultSurround: NodeSeq => NodeSeq = { elems =>
    <lift:surround with="default" at="content">{elems}</lift:surround>
  }

  /** A basic `ContentParser` which handles one template filename suffix,
    * operates on a string, and surrounds the top-level templates with the
    * default surround.
    * @param templateSuffix
    *   the template filename suffix for which this parser will be utilized.
    * @param parseFunction
    *   the parse function for converting the template as a string into a
    *   `NodeSeq`.
    * @param surroundFunction
    *   the function for surrounding the content returned by the
    *   `parseFunction`. See [[defaultSurround]].
    */
  def apply(
      templateSuffix: String,
      parseFunction: String => Box[NodeSeq],
      surroundFunction: NodeSeq => NodeSeq = defaultSurround
  ): ContentParser =
    new ContentParser {
      override def templateSuffixes: Seq[String] = Seq(templateSuffix)
      override def parse(content: InputStream): Box[NodeSeq] =
        toInputStreamParser(parseFunction)(content)
      override def surround(content: NodeSeq): NodeSeq = surroundFunction(
        content
      )
    }

  /** A fully-specified `ContentParser` which handles multiple filename
    * suffixes, operates on an `InputStream`, and surrounds the top-level
    * templates with the default surround
    */
  def apply(
      templateSuffixesSeq: Seq[String],
      parseF: InputStream => Box[NodeSeq],
      surroundF: NodeSeq => NodeSeq
  ): ContentParser =
    new ContentParser {
      override def templateSuffixes: Seq[String] = templateSuffixesSeq
      override def parse(content: InputStream): Box[NodeSeq] = parseF(content)
      override def surround(content: NodeSeq): NodeSeq = surroundF(content)
    }
}
