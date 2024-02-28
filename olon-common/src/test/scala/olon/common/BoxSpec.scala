package olon
package common

import org.scalacheck.Arbitrary
import org.scalacheck.Gen
import org.specs2.ScalaCheck
import org.specs2.mutable.Specification

import Gen._
import Box._

/* commented out because it tests the compilation phase and we want the compiler to "do the right thing"
class TypeBoundsTest extends Specification with ScalaCheck {
  "Type Bounds Spec".title

  "Type bounds" can {
    "do type testing" in {
      def foo[T: ExcludeThisType.exclude[Nothing]#other](a: T) = a.toString
      foo(33.0)
      foo(throw new Exception("foo"))

      true == true
    }
  }
}

 */

/** System under specification for Box.
  */
class BoxSpec extends Specification with ScalaCheck with BoxGenerator {

  "A Box" can {
    "be created from a Option. It is Empty if the option is None" in {
      Box(None) must beEmpty
    }
    "be created from a Option. It is Full(x) if the option is Some(x)" in {
      Box(Some(1)) must_== Full(1)
    }
    "be created from a List containing one element. It is Empty if the list is empty" in {
      Box(Nil) must beEmpty
    }
    "be created from a List containing one element. It is Full(x) if the list is List(x)" in {
      Box(List(1)) must_== Full(1)
    }
    "be created from a List containing more than one element. It is Full(x) if the list is x::rest" in {
      Box(List(1, 2, 3)) must_== Full(1)
    }
    "be used as an iterable" in {
      Full(1) reduceLeft { (x: Int, y: Int) => x + y } must_== 1
    }
    "be used as an Option" in {
      Full(1) orElse Some(2) must beSome(1)
      Empty orElse Some(2) must beSome(2)
    }
    "be implicitly defined from an Option. The openOrThrowException method can be used on an Option for example" in {
      Some(1).openOrThrowException("This is a test") must_== 1
    }
    "be defined from some legacy code (possibly passing null values). If the passed value is not null, a Full(value) is returned" in {
      Box.legacyNullTest("s") must_== Full("s")
    }
    "be defined from some legacy code (possibly passing null values). If the passed value is null, an Empty is returned" in {
      Box.legacyNullTest(null) must_== Empty
    }
  }

  "A Box" should {
    "provide a 'choice' method to either apply a function to the Box value or return another default can" in {
      def gotIt = (x: Int) => Full("got it: " + x.toString)

      Full(1).choice(gotIt)(Full("nothing")) must_== Full("got it: 1")
      Empty.choice(gotIt)(Full("nothing")) must_== Full("nothing")
    }
  }

  "A Full Box" should {
    "not beEmpty" in {
      Full(1).isEmpty must beFalse
    }
    "be defined" in {
      Full(1).isDefined must beTrue
    }
    "return its value when opened" in {
      Full(1).openOrThrowException("This is a test") must_== 1
    }
    "return its value when opened with openOr(default value)" in {
      Full(1).openOr(0) must_== 1
    }
    "return itself when or'ed with another Box" in {
      Full(1).or(Full(2)) must_== Full(1)
    }
    "define an 'exists' method returning true if the Box value satisfies the function" in {
      Full(1) exists { _ > 0 } must beTrue
    }
    "define an exists method returning false if the Box value doesn't satisfy the function" in {
      Full(0) exists { _ > 0 } must beFalse
    }
    "define a forall method returning true if the Box value satisfies the function" in {
      Full(1) forall { _ > 0 } must beTrue
    }
    "define a forall method returning false if the Box value doesn't satisfy the function" in {
      Full(0) forall { _ > 0 } must beFalse
    }
    "define a 'filter' method, returning a Full Box if the filter is satisfied" in {
      Full(1) filter { _ > 0 } must_== Full(1)
    }
    "define a 'filter' method, returning Empty if the filter is not satisfied" in {
      Full(1) filter { _ == 0 } must beEmpty
    }
    "define a 'filterMsg' method, returning a Failure if the filter predicate is not satisfied" in {
      Full(1).filterMsg("not equal to 0")(_ == 0) must_== Failure(
        "not equal to 0",
        Empty,
        Empty
      )
    }
    "define a 'foreach' method using its value (to display it for instance)" in {
      var total = 0
      Full(1) foreach { total += _ }
      total must_== 1
    }
    "define a 'map' method to transform its value" in {
      Full(1) map { _.toString } must_== Full("1")
    }
    "define a 'flatMap' method transforming its value in another Box. If the value is transformed in a Full box, the total result is a Full box" in {
      Full(1) flatMap {
        x: Int =>
          if (x > 0) Full("full") else Empty
      } must_== Full("full")
    }
    "define a 'flatMap' method transforming its value in another Box. If the value is transformed in an Empty box, the total result is an Empty box" in {
      val result = Full(0) flatMap {
        x: Int =>
          if (x > 0) Full("full") else Empty
      }
      result.must(beEmpty)
    }
    "define a 'flatten' method if it contains another Box." in {
      "If the inner box is a Full box, the final result is identical to that box" in {
        Full(Full(1)).flatten must_== Full(1)
      }
      "If the inner box is a Failure, the final result is identical to that box" in {
        Full(Failure("error", Empty, Empty)).flatten must_== Failure(
          "error",
          Empty,
          Empty
        )
      }
      "If the inner box is an Empty box, the final result is identical to that box" in {
        Full(Empty).flatten must_== Empty
      }
    }
    "define a 'collect' method that takes a PartialFunction to transform its contents" in {
      "If the partial-function is defined for the contents of this box, returns a full box containing the result of applying that partial function to this Box's contents" in {
        Full("Albus") collect { case "Albus" => "Dumbledore" } must_== Full(
          "Dumbledore"
        )
      }
      "If the partial-function is not defined for the contents of this box, returns Empty" in {
        Full("Hermione") collect { case "Albus" => "Dumbledore" } must beEmpty
      }
    }
    "define a 'transform' method that takes a PartialFunction to transform this box into another box" in {
      "If the partial-function is defined for this box, returns the result of applying the partial function to it" in {
        Full(404) transform {
          case Full(x: Int) if x != 200 => Failure("Server error")
        } must_== Failure("Server error")
      }
      "If the partial-function is not defined for this box, returns itself unchanged" in {
        Full("Intended Result") transform {
          case _: EmptyBox               => Full("Alternative")
          case Full("Unexpected Result") => Full("Alternative")
        } must_== Full("Intended Result")
      }
    }
    "define a 'flip' method returning Empty" in {
      Full(1) flip { _ => "No data found" } mustEqual Empty
    }
    "define an 'elements' method returning an iterator containing its value" in {
      Full(1).elements.next() must_== 1
    }
    "define a 'toList' method returning a List containing its value" in {
      Full(1).toList must_== List(1)
    }
    "define a 'toOption' method returning a Some object containing its value" in {
      Full(1).toOption must beSome(1)
    }
    "return itself if asked for its status with the operator ?~" in {
      Full(1) ?~ "error" must_== Full(1)
    }
    "return itself if asked for its status with the operator ?~!" in {
      Full(1) ?~! "error" must_== Full(1)
    }
    "define a 'pass' method passing the can to a function and returning itself (alias: $)" in {
      var empty = false
      def emptyString(s: Box[String]) = s foreach {
        c: String =>
          empty = c.isEmpty
      }
      Full("") $ emptyString _
      empty must beTrue
    }
    "define a 'run' method either returning a default value or applying a user-defined function on it" in {
      def appendToString(s: String, x: Int) = s + x.toString
      Full(1).run("string")(appendToString) must_== "string1"
    }
    "define a 'isA' method returning a Full(value) if the value is the instance of a given class" in {
      Full("s").isA(classOf[String]) must_== Full("s")
    }
    "define a 'isA' method returning Empty if the value is not the instance of a given class" in {
      Full("s").isA(classOf[Double]) must_== Empty
    }
    "define a 'asA' method returning a Full(value) if the value is the instance of a given type" in {
      Full("s").asA[String] must_== Full("s")
    }
    "define a 'asA' method returning Empty if the value is not the instance of a given type" in {
      Full("s").asA[Double] must_== Empty
    }

    "define a 'asA' method must work with Boolean" in {
      Full(true).asA[Boolean] must_== Full(true)
      Full(3).asA[Boolean] must_== Empty
    }

    "define a 'asA' method must work with Character" in {
      Full('a').asA[Char] must_== Full('a')
      Full('a').asA[Boolean] must_== Empty
    }

    "define a 'asA' method must work with Byte" in {
      Full(3.toByte).asA[Byte] must_== Full(3.toByte)
      Full(3.toByte).asA[Boolean] must_== Empty
    }

    "define a 'asA' method must work with Double" in {
      Full(44d).asA[Double] must_== Full(44d)
      Full(44d).asA[Boolean] must_== Empty
    }

    "define a 'asA' method must work with Float" in {
      Full(32f).asA[Float] must_== Full(32f)
      Full(33f).asA[Boolean] must_== Empty
    }

    "define a 'asA' method must work with Integer" in {
      Full(3).asA[Int] must_== Full(3)
      Full(3).asA[Boolean] must_== Empty
    }

    "define a 'asA' method must work with Long" in {
      Full(32L).asA[Long] must_== Full(32L)
      Full(32L).asA[Boolean] must_== Empty
    }

    "define a 'asA' method must work with Short" in {
      Full(8.toShort).asA[Short] must_== Full(8.toShort)
      Full(8.toShort).asA[Boolean] must_== Empty
    }

    "not invoke a call-by-name parameter to openOrThrowException" in {
      var sideEffect = false
      def sideEffecting = {
        sideEffect = true
        "This shouldn't have been invoked."
      }

      try {
        Full("hi mom").openOrThrowException(sideEffecting)
      } catch {
        case _: Exception =>
      }

      sideEffect must_== false
    }

  }

  "An Empty Box" should {
    "beEmpty" in {
      Empty.isEmpty must beTrue
    }
    "not be defined" in {
      Empty.isDefined must beFalse
    }
    "throw an exception if opened" in {
      {
        Empty.openOrThrowException(
          "See what happens?, at least we expect it in this case :)"
        ); ()
      } must throwA[NullPointerException]
    }
    "return a default value if opened with openOr" in {
      Empty.openOr(1) must_== 1
    }
    "return the other Box if or'ed with another Box" in {
      Empty.or(Full(1)) must_== Full(1)
    }
    "return itself if filtered with a predicate" in {
      val empty: Box[Int] = Empty
      empty.filter { _ > 0 } must beEmpty
    }
    "define an 'exists' method returning false" in {
      val empty: Box[Int] = Empty
      empty exists { _ > 0 } must beFalse
    }
    "define a 'forall' method returning true" in {
      val empty: Box[Int] = Empty
      empty forall { _ > 0 } must beTrue
    }
    "define a 'filter' method, returning Empty" in {
      val empty: Box[Int] = Empty
      empty filter { _ > 0 } must beEmpty
    }
    "define a 'filterMsg' method, returning a Failure" in {
      Empty.filterMsg("not equal to 0")(_ == 0) must_== Failure(
        "not equal to 0",
        Empty,
        Empty
      )
    }
    "define a 'foreach' doing nothing" in {
      var total = 0
      val empty: Box[Int] = Empty
      empty foreach { total += _ }
      total must_== 0
    }
    "define a 'map' method returning Empty" in {
      Empty map { _.toString } must beEmpty
    }
    "define a 'flatMap' method returning Empty" in {
      Empty flatMap { _: Int => Full("full") } must beEmpty
    }
    "define a 'flatten' method returning Empty" in {
      Empty.flatten must beEmpty
    }
    "define a 'collect' method returning Empty" in {
      Empty collect { case _ => "Some Value" } must beEmpty
    }
    "define a 'transform' method that takes a PartialFunction to transform this Empty box into another box" in {
      "If the partial-function is defined for Empty, returns the result of applying the partial function to it" in {
        Empty transform {
          case Failure("error", Empty, Empty) => Full("failure-alternative")
          case Empty                          => Full("alternative")
        } must_== Full("alternative")
      }
      "If the partial-function is not defined for Empty, returns Empty" in {
        Empty transform { case Failure("The Phantom Menace", Empty, Empty) =>
          Full("Return Of The Jedi")
        } must_== Empty
      }
    }
    "define a 'flip' method returning a Full box" in {
      Empty flip {
        case Empty => "flipped-empty"
        case _     => "flipped-failure"
      } mustEqual Full("flipped-empty")
    }
    "define an 'elements' method returning an empty iterator" in {
      Empty.elements.hasNext must beFalse
    }
    "define a 'toList' method returning Nil" in {
      Empty.toList must_== Nil
    }
    "define a 'toOption' method returning None" in {
      Empty.toOption must beNone
    }
    "return a failure with a message if asked for its status with the operator ?~" in {
      Empty ?~ "nothing" must_== Failure("nothing", Empty, Empty)
    }
    "return a failure with a message if asked for its status with the operator ?~!" in {
      Empty ?~! "nothing" must_== Failure("nothing", Empty, Empty)
    }
    "define a 'isA' method returning Empty" in {
      Empty.isA(classOf[Double]) must_== Empty
    }
    "define a 'asA' method returning Empty" in {
      Empty.asA[Double] must_== Empty
    }

    "invoke a call-by-name parameter to openOrThrowException" in {
      var sideEffect = false
      def sideEffecting = {
        sideEffect = true
        "This should have been invoked."
      }

      try {
        Empty.openOrThrowException(sideEffecting)
      } catch {
        case _: Exception =>
      }

      sideEffect must_== true
    }
  }

  "A Failure is an Empty Box which" can {
    "return its cause as an exception" in {
      case class LiftException(m: String) extends Exception
      Failure(
        "error",
        Full(new LiftException("broken")),
        Empty
      ).exception must_== Full(new LiftException("broken"))
    }
    "return a chained list of causes" in {
      Failure(
        "error",
        Full(new Exception("broken")),
        Full(Failure("nested cause", Empty, Empty))
      ).chain must_== Full(Failure("nested cause", Empty, Empty))
    }
    "be converted to a ParamFailure" in {
      Failure("hi mom") ~> 404 must_== ParamFailure("hi mom", Empty, Empty, 404)
    }
  }

  "A Failure is an Empty Box which" should {
    "return itself if mapped, flatMapped or flattened" in {
      Failure("error", Empty, Empty) map { _.toString } must_== Failure(
        "error",
        Empty,
        Empty
      )
      Failure("error", Empty, Empty) flatMap {
        x: String =>
          Full(x.toString)
      } must_== Failure("error", Empty, Empty)
      Failure("error", Empty, Empty).flatten must_== Failure(
        "error",
        Empty,
        Empty
      )
    }
    "define a 'collect' method returning itself" in {
      Failure("error", Empty, Empty) collect { case _ =>
        "Some Value"
      } must_== Failure("error", Empty, Empty)
    }
    "define a 'transform' method that takes a PartialFunction to transform this Failure into another box" in {
      "If the partial-function is defined for this Failure, returns the result of applying the partial function to it" in {
        Failure("The Phantom Menace") transform {
          case Failure("The Phantom Menace", Empty, Empty) =>
            Full("Return Of The Jedi")
        } must_== Full("Return Of The Jedi")

        Failure("The Phantom Menace") transform {
          case Failure("The Phantom Menace", Empty, Empty) => Failure("Clones")
          case _                                           => Full("Jedi")
        } must_== Failure("Clones")
      }
      "If the partial-function is not defined for this Failure, returns itself unchanged" in {
        Failure("Clones") transform {
          case Failure("The Phantom Menace", Empty, Empty) => Full("Jedi")
        } must_== Failure("Clones")
      }
    }
    "define a 'flip' method returning a Full box" in {
      Failure("error", Empty, Empty) flip {
        case Empty      => "flipped-empty"
        case _: Failure => "flipped-failure"
      } must_== Full("flipped-failure")
    }
    "return itself when asked for its status with the operator ?~" in {
      Failure("error", Empty, Empty) ?~ "nothing" must_== Failure(
        "error",
        Empty,
        Empty
      )
    }
    "create a new failure with a chained message if asked for its status with the operator ?~!" in {
      Failure("error", Empty, Empty) ?~! "error2" must_== Failure(
        "error2",
        Empty,
        Full(Failure("error", Empty, Empty))
      )
    }
    "return false for exist method" in {
      Failure("error", Empty, Empty) exists { _ => true } must beFalse
    }
    "return true for forall method" in {
      Failure("error", Empty, Empty) forall { _ => false } must beTrue
    }
  }

  "A ParamFailure is a failure which" should {
    "appear in the chain when ~> is invoked on it" in {
      Failure("Apple") ~> 404 ~> "apple" must_==
        ParamFailure(
          "Apple",
          Empty,
          Full(
            ParamFailure("Apple", Empty, Empty, 404)
          ),
          "apple"
        )
    }
  }

  "A Box equals method" should {

    "return true with comparing two identical Box messages" in prop {
      (c1: Box[Int], c2: Box[Int]) =>
        (c1, c2) match {
          case (Empty, Empty)     => c1 must_== c2
          case (Full(x), Full(y)) => (c1 == c2) must_== (x == y)
          case (Failure(m1, e1, l1), Failure(m2, e2, l2)) =>
            (c1 == c2) must_== ((m1, e1, l1) == (m2, e2, l2))
          case _ => c1 must be_!=(c2)
        }
    }

    "return false with comparing one Full and another object" in {
      Full(1) must be_!=("hello")
    }

    "return false with comparing one Empty and another object" in {
      Empty must be_!=("hello")
    }

    "return false with comparing one Failure and another object" in {
      Failure("", Empty, Empty) must be_!=("hello")
    }
  }

  "A List[Box[T]]" should {
    "be convertable to a Box[List[T]] when all are Full" in {
      val someBoxes: List[Box[String]] = List(Full("bacon"), Full("sammich"))
      val singleBox = someBoxes.toSingleBox("Box failed!")

      singleBox must_== Full(List("bacon", "sammich"))
    }

    "be convertable to a Box[List[T]] when some are Full and some are Empty" in {
      val someBoxes: List[Box[String]] =
        List(Full("bacon"), Full("sammich"), Empty)
      val singleBox = someBoxes.toSingleBox("Box failed!")

      singleBox must_== Full(List("bacon", "sammich"))
    }

    "be convertable to a ParamFailure[Box[List[T]]] when any are Failure" in {
      val someBoxes: List[Box[String]] =
        List(Full("bacon"), Full("sammich"), Failure("I HATE BACON"))
      val singleBox =
        someBoxes.toSingleBox("This should be in the param failure.")

      singleBox must beLike { case ParamFailure(message, _, _, _) =>
        message must_== "This should be in the param failure."
      }
    }

    "chain the ParamFailure to the failures in the list when any are Failure" in {
      val someBoxes: List[Box[String]] = List(
        Full("bacon"),
        Failure("I HATE BACON"),
        Full("sammich"),
        Failure("MORE BACON FAIL"),
        Failure("BACON WHY U BACON")
      )

      val singleBox = someBoxes.toSingleBox("Failure.")

      val expectedChain =
        Failure(
          "I HATE BACON",
          Empty,
          Full(
            Failure(
              "MORE BACON FAIL",
              Empty,
              Full(Failure("BACON WHY U BACON"))
            )
          )
        )

      singleBox must beLike { case ParamFailure(_, _, chain, _) =>
        chain must_== Full(expectedChain)
      }
    }
  }

  "A Box tryo method" should {
    "return Full" in {
      Box.tryo(1) must_== Full(1)
    }

    "return Failure(_, Full(NPE), _) in case of NPE" in {
      val obj: Object = null

      Box.tryo(obj.toString) must beLike { case Failure(_, Full(ex), _) =>
        ex.getClass must_== classOf[NullPointerException]
      }
    }

    "return Empty in case of NPE and ignore list with NPE" in {
      val ignore: List[Class[_]] = List(classOf[NullPointerException])

      Box.tryo(ignore)(throw new NullPointerException) must_== Empty
    }

    "return Failure(_, Full(NPE), _) in case of non empty ignore list without NPE" in {
      val ignore: List[Class[_]] = List(classOf[IllegalArgumentException])

      Box.tryo(ignore)(throw new NullPointerException) must beLike {
        case Failure(_, Full(ex), _) =>
          ex.getClass must_== classOf[NullPointerException]
      }
    }

    "not throw NPE in case of nullable ignore list" in {
      val ignore: List[Class[_]] = null

      Box.tryo(ignore)(throw new IllegalArgumentException) must beLike {
        case Failure(_, Full(ex), _) =>
          ex.getClass must_== classOf[IllegalArgumentException]
      }
    }
  }
}

trait BoxGenerator {

  implicit def genThrowable: Arbitrary[Throwable] = Arbitrary[Throwable] {
    case object UserException extends Throwable
    const(UserException)
  }

  implicit def genBox[T](implicit a: Arbitrary[T]): Arbitrary[Box[T]] =
    Arbitrary[Box[T]] {
      frequency(
        (3, const(Empty)),
        (3, a.arbitrary.map(Full[T])),
        (1, genFailureBox)
      )
    }

  def genFailureBox: Gen[Failure] = for {
    msgLen <- choose(0, 4)
    msg <- listOfN(msgLen, alphaChar)
    exception <- const(Full(new Exception("")))
    chainLen <- choose(1, 5)
    chain <- frequency((1, listOfN(chainLen, genFailureBox)), (3, const(Nil)))
  } yield Failure(msg.mkString, exception, Box(chain.headOption))

}
