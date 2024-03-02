package olon
package json

import org.scalacheck._
import org.specs2.ScalaCheck
import org.specs2.mutable.Specification

import Arbitrary._
import Prop.{forAll, forAllNoShrink}

class JsonAstSpec extends Specification with JValueGen with ScalaCheck {
  "Functor identity" in {
    val identityProp = (json: JValue) => json == (json.map(identity))
    forAll(identityProp)
  }

  "Functor composition" in {
    val compositionProp =
      (json: JValue, fa: JValue => JValue, fb: JValue => JValue) => {
        json.map(fb).map(fa) == json.map(fa compose fb)
      }

    forAll(compositionProp)
  }.pendingUntilFixed(
    "Requires a fundamental change to map; see https://github.com/lift/framework/issues/1816 ."
  )

  "Monoid identity" in {
    val identityProp =
      (json: JValue) => (json ++ JNothing == json) && (JNothing ++ json == json)
    forAll(identityProp)
  }

  "Monoid associativity" in {
    val assocProp =
      (x: JValue, y: JValue, z: JValue) => x ++ (y ++ z) == (x ++ y) ++ z
    forAll(assocProp)
  }

  "Merge identity" in {
    val identityProp = (json: JValue) =>
      (json.merge(JNothing)) == json && (JNothing.merge(json)) == json
    forAll(identityProp)
  }

  "Merge idempotency" in {
    val idempotencyProp = (x: JValue) => (x.merge(x)) == x
    forAll(idempotencyProp)
  }

  "Diff identity" in {
    val identityProp = (json: JValue) =>
      (json.diff(JNothing)) == Diff(JNothing, JNothing, json) &&
        (JNothing.diff(json)) == Diff(JNothing, json, JNothing)

    forAll(identityProp)
  }

  "Diff with self is empty" in {
    val emptyProp =
      (x: JValue) => (x.diff(x)) == Diff(JNothing, JNothing, JNothing)
    forAll(emptyProp)
  }

  "Diff is subset of originals" in {
    val subsetProp = (x: JObject, y: JObject) => {
      val Diff(c, a, _) = x.diff(y)
      y == (y.merge((c.merge(a))))
    }
    forAll(subsetProp)
  }

  "Diff result is same when fields are reordered" in {
    val reorderProp = (x: JObject) =>
      (x.diff(reorderFields(x))) == Diff(JNothing, JNothing, JNothing)
    forAll(reorderProp)
  }

  "Remove all" in {
    val removeAllProp = (x: JValue) => (x remove { _ => true }) == JNothing
    forAll(removeAllProp)
  }

  "Remove nothing" in {
    val removeNothingProp = (x: JValue) => (x remove { _ => false }) == x
    forAll(removeNothingProp)
  }

  // SCALA3 Using `?` instead of `_`
  "Remove removes only matching elements" in {
    forAllNoShrink(genJValue, genJValueClass) {
      (json: JValue, x: Class[? <: JValue]) =>
        {
          val removed = json.remove(typePredicate(x))
          val Diff(c, a, _) = json.diff(removed)
          val elemsLeft = removed filter { case _ =>
            true
          }
          c == JNothing && a == JNothing && elemsLeft.forall(_.getClass != x)
        }
    }
  }

  "Replace one" in {
    val anyReplacement = (x: JValue, replacement: JObject) => {
      def findOnePath(jv: JValue, l: List[String]): List[String] = jv match {
        case JObject(fl) =>
          fl match {
            case field :: _ => findOnePath(field.value, l)
            case Nil        => l
          }
        case _ => l
      }

      val path = findOnePath(x, Nil).reverse
      val result = x.replace(path, replacement)

      def replaced(path: List[String], in: JValue): Boolean = {
        path match {
          case Nil => x == in

          case name :: Nil =>
            (in \ name) match {
              case `replacement` => true
              case _             => false
            }

          case name :: xs =>
            (in \ name) match {
              case JNothing => false
              case value    => replaced(xs, value)
            }
        }
      }

      replaced(path, result)
    }

    // ensure that we test some JObject instances
    val fieldReplacement =
      (x: JObject, replacement: JObject) => anyReplacement(x, replacement)

    forAll(fieldReplacement)
    forAll(anyReplacement)
  }

  "allow escaping arbitrary characters when serializing" in {
    JsonAST.render(
      JString("aaabbb"),
      JsonAST.RenderSettings(0, Set('c'))
    ) must not be matching("a".r)
  }

  "escape bad JSON characters by default" in {
    val allCharacters: String =
      ('\u0000' to '\uffff').mkString("")

    val rendered =
      JsonAST.render(
        JString(allCharacters),
        JsonAST.RenderSettings.compact
      )

    "[\u0000-\u0019]".r.pattern
      .matcher(rendered)
      .find() must beFalse
  }

  "allow escaping bad JavaScript characters when serializing" in {
    val allCharacters =
      ('\u0000' to '\uffff').mkString("")

    val rendered =
      JsonAST.render(
        JString(allCharacters),
        JsonAST.RenderSettings.compactJs
      )

    "[\u0000\u00ad\u0600-\u0604\u070f\u17b4\u17b5\u200c-\u200f\u2028-\u202f\u2060-\u206f\ufeff\ufff0-\uffff]".r.pattern
      .matcher(rendered)
      .find() must beFalse
  }

  "equals hashCode" in prop({ (x: JObject) =>
    val y = JObject(scala.util.Random.shuffle(x.obj))

    x must_== y
    x.## must_== y.##
  })

  "find all children" in {
    val subject = JObject(
      JField("alpha", JString("apple")) ::
        JField(
          "beta",
          JObject(
            JField("alpha", JString("bacon")) ::
              JField("charlie", JString("i'm a masseuse")) ::
              Nil
          )
        ) ::
        Nil
    )

    subject \\ "alpha" must_==
      JObject(
        JField("alpha", JString("apple")) ::
          JField("alpha", JString("bacon")) ::
          Nil
      )
    subject \\ "charlie" must_== JObject(
      List(JField("charlie", JString("i'm a masseuse")))
    )
  }

  private def reorderFields(json: JValue) = json map {
    case JObject(xs) => JObject(xs.reverse)
    case x           => x
  }

  // SCALA3 Using `?` instead of `_`
  private def typePredicate(clazz: Class[?])(json: JValue) = json match {
    case x if x.getClass == clazz => true
    case _                        => false
  }

  implicit def arbJValue: Arbitrary[JValue] = Arbitrary(genJValue)
  implicit def arbJObject: Arbitrary[JObject] = Arbitrary(genObject)
  implicit val arbJValueFn: Arbitrary[JValue => JValue] = Arbitrary(genJValueFn)
}
