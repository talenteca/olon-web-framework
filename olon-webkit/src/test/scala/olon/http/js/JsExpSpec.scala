package olon
package http
package js

import org.specs2.mutable.Specification

import common._
import json._
import JsonDSL._

/** System under specification for JsExp.
  */
class JsExpSpec extends Specification {
  "JsExp Specification".title

  "JsExp" should {
    "Deal with lift-json" in {
      val json = ("a" -> 4) ~ ("b" -> 44)

      JE.JsArray(json, "dog")
        .toJsCmd must_== ("""[{"a":4,"b":44}, "dog"]""" + "\n")

    }

    "Implicitly convert from Numeric types" in {
      import JsExp._

      (42: JsExp) must_== JE.Num(42)
      (42L: JsExp) must_== JE.Num(42L)
      (42.0: JsExp) must_== JE.Num(42.0)
      (42.0f: JsExp) must_== JE.Num(42.0f)
    }

    "Correctly infer type" in {
      val l: List[Option[Double]] = List(Some(1), None)

      import JsExp._

      // Can't get this to work:  JE.JsArray(l map {d => (d.getOrElse(0.0)):JsExp}) must_== JE.JsArray(1.0, 0.0)
      JE.JsArray(l map { d => (d.getOrElse(0.0): Double): JsExp }) must_== JE
        .JsArray(1.0, 0.0)
    }

  }

  "JsExp ~>" should {
    import JE._

    val js = JsVar("myVar")

    "handle Full parameters" in {
      val expected = (js ~> JsFunc("x") ~> JsFunc("y")).toJsCmd
      (js ~> Full(JsFunc("x")) ~> JsFunc("y")).toJsCmd must_== expected
      (js ~> Some(JsFunc("x")) ~> JsFunc("y")).toJsCmd must_== expected
    }

    "ignore Empty parameters" in {
      val expected = (js ~> JsFunc("x")).toJsCmd
      (js ~> Empty ~> JsFunc("x")).toJsCmd must_== expected
      (js ~> None ~> JsFunc("x")).toJsCmd must_== expected
    }
  }

  "JsArray" should {
    "work with varags" in {
      JE.JsArray(2, "x", 42.0).toJsCmd must_== "[2, \"x\", 42.0]\n"
    }

    "work with lists" in {
      val l: List[JsExp] = List(2, "x", 42.0)
      JE.JsArray(l).toJsCmd must_== "[2, \"x\", 42.0]\n"
    }
  }
}
