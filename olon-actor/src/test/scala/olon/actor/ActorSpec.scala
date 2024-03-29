package olon
package actor

import org.specs2.mutable.Specification

import scala.concurrent.duration._

import common._

/** Systems under specification for Lift Actor.
  */
class ActorSpec extends Specification {
  "Actor Specification".title
  sequential

  "A Scala Actor" should {
    "support common features" in commonFeatures(new MyScalaActor)
  }

  private def commonFeatures(actor: LiftActor) = {
    sequential

    "allow setting and getting of a value" in {
      val a = actor
      a ! Set(33)
      a !? Get()
      (a.!?(50, Get())) must be_==(Full(Answer(33)))
        .eventually(900, 100.milliseconds)
    }

    "allow setting and getting of a value with subclass of Get()" in {
      val a = actor
      a ! Set(33)
      a ! new FunnyGet()
      (a.!?(50L, new FunnyGet())) must be_==(Full(Answer(33)))
        .eventually(900, 100.milliseconds)
    }

    "allow adding of a value" in {
      val a = actor
      a ! Set(33)
      (a !< Add(44)).get(500) must be_==(Full(Answer(77)))
    }

    "allow subtracting of a value" in {
      val a = actor
      a ! Set(33)
      (a !< Sub(11)).get(500) must be_==(Full(Answer(22)))
    }

    "properly timeout" in {
      val a = actor
      (a !< Set(33)).get(50) must be_==(Empty).eventually(900, 100.milliseconds)
    }
  }

}

case class Add(num: Int)
case class Sub(num: Int)
case class Set(num: Int)

case class Get()
class FunnyGet() extends Get()

case class Answer(num: Int)
