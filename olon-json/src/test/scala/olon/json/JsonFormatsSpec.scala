package olon
package json

import org.specs2.mutable.Specification

/** System under specification for JSON Formats.
  */
class JsonFormatsSpec extends Specification with TypeHintExamples {
  "JsonFormats Specification".title

  implicit val formats: Formats =
    ShortTypeHintExamples.formats + FullTypeHintExamples.formats.typeHints

  val hintsForFish =
    ShortTypeHintExamples.formats.typeHints.hintFor(classOf[Fish])
  val hintsForDog =
    ShortTypeHintExamples.formats.typeHints.hintFor(classOf[Dog])
  val hintsForAnimal =
    FullTypeHintExamples.formats.typeHints.hintFor(classOf[Animal])

  "hintsFor across composite formats" in {
    (formats.typeHints.hintFor(classOf[Fish]) mustEqual (hintsForFish)) and
      (formats.typeHints.hintFor(classOf[Dog]) mustEqual (hintsForDog)) and
      (formats.typeHints.hintFor(classOf[Animal]) mustEqual (hintsForAnimal))
  }

  "classFor across composite formats" in {
    (formats.typeHints.classFor(
      hintsForFish
    ) mustEqual (ShortTypeHintExamples.formats.typeHints.classFor(
      hintsForFish
    ))) and
      (formats.typeHints.classFor(
        hintsForDog
      ) mustEqual (ShortTypeHintExamples.formats.typeHints.classFor(
        hintsForDog
      ))) and
      (formats.typeHints.classFor(
        hintsForAnimal
      ) mustEqual (FullTypeHintExamples.formats.typeHints.classFor(
        hintsForAnimal
      )))
  }

  // SCALA3 Using `?` instead of `_`
  "parameter name reading strategy can be changed" in {
    object TestReader extends ParameterNameReader {
      def lookupParameterNames(constructor: java.lang.reflect.Constructor[?]) =
        List("name", "age")
    }
    implicit val formats = new DefaultFormats {
      override val parameterNameReader = TestReader
    }
    val json = parse("""{"name":"joe","age":35}""")
    json.extract[NamesNotSameAsInJson] mustEqual NamesNotSameAsInJson("joe", 35)
  }
}

case class NamesNotSameAsInJson(n: String, a: Int)
