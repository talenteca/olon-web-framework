package olon
package common

import org.specs2.mutable.Specification


/**
 * System under specification for Heterogeneous List.
 */
class HListSpec extends Specification {

  "An HList" should {

    "get the types right" in {
      import HLists._

      val x = 1 :+: "Foo" :+: HNil

      val head: Int = x.head
      val head2: String = x.tail.head

      x.head must_== 1
      x.tail.head must_== "Foo"
    }

    "properly report its length" in {
      import HLists._

      val x = 1 :+: "Foo" :+: HNil

      HNil.length must_== 0
      x.length must_== 2
      ("Bam" :+: x).length must_== 3
    }
  }

  "A combinable box" should {

    "have a box built with a failure result in a failure" in {
      import CombinableBox._

      val x = Full("a") :&: Full(1) :&: Empty

      // result in a failure
      x match {
        case Left(_) => success
        case _       => failure
      }
    }

    "be able to build a box with all the Full elements matching" in {
      import CombinableBox._
      import HLists._

      val x = Full("a") :&: Full(1) :&: Full(List(1,2,3))

      // result in a failure
      x match {
        case Right(a :+: one :+: lst :+:HNil) => {
          // val a2: Int = a  fails... not type safe

          val as: String = a
          val onei: Int = one
          val lstl: List[Int] = lst

          success
        }

        case Right(_) => failure
        case Left(_) => failure
      }
    }

    "be usable in for comprehension" in {
      import CombinableBox._
      import HLists._

      val res = for {
        a :+: one :+: lst :+: _ <-
        (Full("a") ?~ "Yak" :&: Full(1) :&: Full(List(1,2,3))) ?~! "Dude"
      } yield a.length * one * lst.foldLeft(1)(_ * _)

      res must_== Full(6)
    }
  }

}

