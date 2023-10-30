package olon
package json

import scala.reflect.Manifest

/** Functions to serialize and deserialize a case class. Custom serializer can
  * be inserted if a class is not a case class. <p> Example:<pre> val hints =
  * new ShortTypeHints( ... ) implicit val formats =
  * Serialization.formats(hints) </pre>
  *
  * @see
  *   olon.json.TypeHints
  */
object Serialization {
  import java.io.{Reader, StringWriter, Writer}

  /** Serialize to String.
    */
  def write[A <: Any](a: A)(implicit formats: Formats): String =
    compactRender(Extraction.decompose(a)(formats))

  /** Serialize to Writer.
    */
  def write[A <: Any, W <: Writer](a: A, out: W)(implicit
      formats: Formats
  ): W = {
    JsonAST.compactRender(Extraction.decompose(a)(formats), out)
    out
  }

  /** Serialize to String (pretty format).
    */
  def writePretty[A <: Any](a: A)(implicit formats: Formats): String =
    (writePretty(a, new StringWriter)(formats)).toString

  /** Serialize to Writer (pretty format).
    */
  def writePretty[A <: Any, W <: Writer](a: A, out: W)(implicit
      formats: Formats
  ): W = {
    JsonAST.prettyRender(Extraction.decompose(a)(formats), out)
    out
  }

  /** Deserialize from a String.
    */
  def read[A](json: String)(implicit formats: Formats, mf: Manifest[A]): A =
    parse(json).extract(formats, mf)

  /** Deserialize from a Reader.
    */
  def read[A](in: Reader)(implicit formats: Formats, mf: Manifest[A]): A =
    JsonParser.parse(in).extract(formats, mf)

  /** Create Serialization formats with given type hints. <p> Example:<pre> val
    * hints = new ShortTypeHints( ... ) implicit val formats =
    * Serialization.formats(hints) </pre>
    */
  def formats(hints: TypeHints) = new Formats {
    val dateFormat = DefaultFormats.lossless.dateFormat
    override val typeHints = hints
  }
}
