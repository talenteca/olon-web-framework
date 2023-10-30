package olon
package http

import org.specs2.mutable.Specification

import common._

/** System under specification for FactoryMaker.
  */
class FactoryMakerSpec extends Specification {
  "FactoryMaker Specification".title

  object MyFactory extends Factory {
    val f1: FactoryMaker[List[String]] = new FactoryMaker(() =>
      List("Hello", "World")
    ) {}
    val f2: FactoryMaker[Boolean] = new FactoryMaker(() => false) {}
  }

  "Factories" should {
    "Allow multiple FactoryMakers to exist" in {
      val session = new LiftSession("hello", "", Empty)

      val res = S.initIfUninitted(session) {
        MyFactory.f2.request.set(true)

        MyFactory.f1.vend
      }
      res must_== List("Hello", "World")
    }
  }
}
