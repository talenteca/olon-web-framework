package olon
package util

import org.scalacheck.Arbitrary
import org.scalacheck.Gen
import org.scalacheck.Prop
import org.specs2.ScalaCheck
import org.specs2.mutable.Specification

import scala.util.parsing.combinator.Parsers

import Gen._
import Prop._

class CombParserHelpersSpec extends Specification with ScalaCheck {
  "CombParserHelpers Specification".title

  object ParserHelpers extends CombParserHelpers with Parsers
  import ParserHelpers._

  "The parser helpers" should {
    "provide an isEof function returning true iff a char is end of file" in {
      isEof('\u001a') must beTrue
    }
    "provide an notEof function returning true iff a char is not end of file" in {
      notEof('\u001a') must beFalse
    }
    "provide an isNum function returning true iff a char is a digit" in {
      isNum('0') must beTrue
    }
    "provide an notNum function returning true iff a char is not a digit" in {
      notNum('0') must beFalse
    }
    "provide an wsc function returning true iff a char is a space character" in {
      List(' ', '\t', '\r', '\n') foreach { wsc(_) must beTrue }
      wsc('a') must beFalse
    }
    "provide a whitespace parser: white. Alias: wsc" in {
      import WhiteStringGen._
      // SCALA3 Using `?` instead of `_`
      val whiteParse = (s: String) => wsc(s).isInstanceOf[Success[?]]
      forAll(whiteParse)
    }
    "provide a whiteSpace parser always succeeding and discarding its result" in {
      import StringWithWhiteGen._
      val whiteSpaceParse =
        (s: String) =>
          whiteSpace(s) must beLike { case Success(x, _) =>
            x.toString must_== "()"
          }
      forAll(whiteSpaceParse)
    }
    "provide an acceptCI parser to parse whatever string matching another string ignoring case" in {
      val ignoreCaseStringParse: Function2[String, String, Boolean] =
        (s: String, s2: String) =>
          acceptCI(s).apply(s2) match {
            case Success(_, _) => s2.toUpperCase must startWith(s.toUpperCase)
            case _             => true
          }
      forAll(ignoreCaseStringParse)
    }

    "provide a digit parser - returning a String" in {
      val isDigit: String => Boolean =
        (s: String) =>
          digit(s) match {
            case Success(_, _) => s must beMatching("(?s)\\p{Nd}.*")
            case _             => true
          }
      forAll(isDigit)
    }
    "provide an aNumber parser - returning an Int if succeeding" in {
      val number: String => Boolean =
        (s: String) => {
          aNumber(s) match {
            case Success(_, _) => s must beMatching("(?s)\\p{Nd}+.*")
            case _             => true
          }
        }
      forAll(number)
    }

    "provide a slash parser" in {
      slash("/").get must_== '/'
      slash("x") must beLike { case Failure(_, _) => 1 must_== 1 }
    }
    "provide a colon parser" in {
      colon(":").get must_== ':'
      colon("x") must beLike { case Failure(_, _) => 1 must_== 1 }
    }
    "provide a EOL parser which parses the any and discards any end of line character" in {
      List("\n", "\r") map { s =>
        val result = EOL(s)
        result.get.toString must_== "()"
        result.next.atEnd must beTrue
      }

      success
    }
    val parserA = elem("a", (c: Char) => c == 'a')
    val parserB = elem("b", (c: Char) => c == 'b')
    val parserC = elem("c", (c: Char) => c == 'c')
    val parserD = elem("d", (c: Char) => c == 'd')
    def shouldSucceed[T](r: ParseResult[T]) = r match {
      case Success(_, _) => true
      case _             => false
    }
    "provide a permute parser succeeding if any permutation of given parsers succeeds" in {
      def permuteParsers(s: String) =
        shouldSucceed(permute(parserA, parserB, parserC, parserD)(s))
      val permutationOk = (s: String) => permuteParsers(s)

      forAll(AbcdStringGen.abcdString)(permutationOk)
    }
    "provide a permuteAll parser succeeding if any permutation of the list given parsers, or a sublist of the given parsers succeeds" in {
      def permuteAllParsers(s: String) =
        shouldSucceed(permuteAll(parserA, parserB, parserC, parserD)(s))
      // SCALA3 Adding declaration type for using implicit correctly
      implicit def pick3Letters: Arbitrary[String] =
        AbcdStringGen.pickN(3, List("a", "b", "c"))

      forAll { (s: String) =>
        ((new scala.collection.immutable.StringOps(
          s
        )).nonEmpty) ==> permuteAllParsers(s)
      }
    }
    "provide a repNN parser succeeding if an input can be parsed n times with a parser" in {
      def repNNParser(s: String) = shouldSucceed(repNN(3, parserA)(s))
      // SCALA3 Adding declaration type for using implicit correctly
      implicit def pick3Letters: Arbitrary[String] =
        AbcdStringGen.pickN(3, List("a", "a", "a"))

      forAll { (s: String) =>
        ((new scala.collection.immutable.StringOps(
          s
        )).nonEmpty) ==> repNNParser(s)
      }
    }
  }
}

object AbcdStringGen {
  implicit def abcdString: Gen[String] =
    for (
      len <- choose(4, 4);
      string <- pick(len, List("a", "b", "c", "d"))
    ) yield string.mkString("")

  def pickN(n: Int, elems: List[String]) =
    Arbitrary { for (string <- pick(n, elems)) yield string.mkString("") }
}

object WhiteStringGen {
  def genWhite =
    for (
      len <- choose(1, 4);
      string <- listOfN(
        len,
        frequency(
          (1, Gen.const(" ")),
          (1, Gen.const("\t")),
          (1, Gen.const("\r")),
          (1, Gen.const("\n"))
        )
      )
    ) yield string.mkString("")

  implicit def genWhiteString: Arbitrary[String] =
    Arbitrary { genWhite }
}

object StringWithWhiteGen {
  import WhiteStringGen._

  def genStringWithWhite =
    for (
      len <- choose(1, 4);
      string <- listOfN(
        len,
        frequency((1, Gen.const("a")), (2, Gen.const("b")), (1, genWhite))
      )
    ) yield string.mkString("")

  implicit def genString: Arbitrary[String] =
    Arbitrary { genStringWithWhite }
}
