package olon
package util

import org.specs2.matcher.DataTables
import org.specs2.mutable.Specification

import java.io.ByteArrayInputStream

import common._
import BasicTypesHelpers._

/** Systems under specification for BasicTypesHelpers.
  */
class BasicTypesHelpersSpec
    extends Specification
    with DataTables
    with Loggable {
  "BasicTypesHelpers Specification".title

  "Basic types helpers" should {

    "be lazy" in {
      (false.?[Int]({ throw new Exception("Bummer") }).|(3)) must_== 3
      (true.?[Int](3).|({ throw new Exception("Bummer") })) must_== 3
    }

    "provide a ternary operator: (condition) ? A | B" in {
      (1 == 2) ? "a" | "b" must_== "b"
    }

    "provide a ?> operator to add an element to a list if an expression is true" in {
      (1 == 1) ?> "a" ::: List("b") must_== List("a", "b")
      (1 == 2) ?> "a" ::: List("b") must_== List("b")
    }
    val failure = Failure(null, null, null)
    "have a toBoolean method converting any object to a reasonable Boolean value" in {
      toBoolean(null) must_== false
      "object value" || "boolean value" |
        (0: Any) !! false |
        1 !! true |
        true !! true |
        false !! false |
        "" !! false |
        "string" !! false |
        "t" !! true |
        "total" !! false |
        "T" !! true |
        "This" !! false |
        "0" !! false |
        "on" !! true |
        None !! false |
        Some("t") !! true |
        Empty !! false |
        Full("t") !! true |
        failure !! false |
        List("t", "f") !! true |> { (o: Any, result: Boolean) =>
          toBoolean(o) must_== result
        }
    }

    "have a AsBoolean extractor converting any object to a reasonable Boolean value" in {
      "object value" || "boolean value" |>
        "t" !! Some(true) |
        "" !! None |
        "string" !! None |
        "total" !! None |
        "T" !! Some(true) |
        "This" !! None |
        "0" !! Some(false) | { (o: String, result: Option[Boolean]) =>
          AsBoolean.unapply(o) must_== result
        }
    }

    "have an AsInt extractor converting any String to a reasonable Int value" in {
      "object value" || "int value" |>
        "3" !! Some(3) |
        "n" !! None | { (o: String, result: Option[Int]) =>
          AsInt.unapply(o) must_== result
        }
    }

    "have an AsLong extractor converting any String to a reasonable Long value" in {
      "object value" || "long value" |>
        "3" !! Some(3L) |
        "n" !! None | { (o: String, result: Option[Long]) =>
          AsLong.unapply(o) must_== result
        }
    }

    "have a toInt method converting any object to a reasonable Int value" in {
      def date(t: Int) = new _root_.java.util.Date(t)
      toInt(null) must_== 0
      "object value" || "int value" |>
        1 !! 1 |
        1L !! 1 |
        List(1, 2) !! 1 |
        Some(1) !! 1 |
        Full(1) !! 1 |
        None !! 0 |
        Empty !! 0 |
        failure !! 0 |
        "3" !! 3 |
        "n" !! 0 |
        date(3000) !! 3 | { (o: Any, result: Int) =>
          toInt(o) must_== result
        }
    }

    "have a toLong method converting any object to a reasonable Long value" in {
      def date(t: Int) = new _root_.java.util.Date(t)
      toLong(null) must_== 0L
      "object value" || "long value" |>
        1 !! 1L |
        1L !! 1L |
        List(1, 2) !! 1L |
        Some(1) !! 1L |
        Full(1) !! 1L |
        None !! 0L |
        Empty !! 0L |
        failure !! 0L |
        "3" !! 3L |
        "n" !! 0L |
        date(3000) !! 3000L | { (o: Any, result: Long) =>
          toLong(o) must_== result
        }
    }

    "have a toByteArrayInputStream reading an InputStream to a ByteArrayInputStream" in {
      val array: Array[Byte] = Array(12, 14)
      val input = new ByteArrayInputStream(array)
      val result = toByteArrayInputStream(input)
      result.read must_== 12
      result.read must_== 14
    }
    "have a isEq method comparing 2 Byte arrays and returning true if they contain the same elements" in {
      val a: Array[Byte] = Array(12, 14)
      val b: Array[Byte] = Array(12, 14)
      val c: Array[Byte] = Array(12, 13)
      isEq(a, b) must beTrue
      isEq(a, c) must beFalse
    }
    "have a notEq method comparing 2 Byte arrays and returning true if they don't contain the same elements" in {
      val a: Array[Byte] = Array(12, 14)
      val b: Array[Byte] = Array(12, 13)
      BasicTypesHelpers.notEq(a, b) must beTrue
    }
  }

  "PartialFunction guard" should {

    "put a guard around a partial function" in {
      val pf1: PartialFunction[String, Unit] = {
        case s if s.startsWith("s") =>
      }

      val pf2: PartialFunction[String, Boolean] = {
        case "snipe" => true
        case "bipe"  => false
      }

      val pf3 = pf1.guard(pf2)
      pf1.guard(pf3)

      pf3.isDefinedAt("bipe") must_== false
      pf3.isDefinedAt("snipe") must_== true
    }
  }

  "AvoidTypeErasure implicit value" should {
    "be in scope" in {
      def f(i: Int)(implicit d: AvoidTypeErasureIssues1) = {
        logger.trace(
          "Avoiding type erasure for class " + d.getClass().getSimpleName()
        )
        i + 1
      }
      f(2) must_== 3
    }
  }

}
