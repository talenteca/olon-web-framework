package olon
package common

/** Via an `[[HLists.HList HList]]` containing a collection of `[[Box]]`, either
  * generates an `HList` of the things (unboxed) or a `List[Failure]`.
  */
object CombinableBox {
  import HLists._

  type Result[A] = Either[List[Failure], A]

  private implicit def emptyBoxToFailure(eb: EmptyBox): Failure = eb match {
    case (f: Failure) => f
    case Empty        => Failure("Empty")
  }

  implicit def boxToCombinableBox[A](in: Box[A]): CombinableBox[A, HNil] =
    CombinableBox(in match {
      case Full(a)      => Right(a :+: HNil)
      case (f: Failure) => Left(f :: Nil)
      case _            => Left(Failure("Empty") :: Nil)
    })

  implicit def boxableToCombinableBox[A](
      in: Boxable[A]
  ): CombinableBox[A, HNil] = in.asBox

  implicit def boxCombinationToCombinableBox[A, B <: HList](
      in: Result[A :+: B]
  ): CombinableBox[A, B] =
    CombinableBox(in)

  implicit def resultToBox[A](result: Result[A]): Box[A] = result match {
    case Left(Nil)      => Empty
    case Left(f :: Nil) => f
    case Left(f) =>
      new ParamFailure("Multiple Failures", Empty, Empty, FailureList(f))
    case Right(x) => Full(x)
  }

  /** If the `[[Failure]]` is going to be condensed, a `FailureList` is
    * generated to allow type-safe pattern matches without worrying about
    * erasure.
    */
  final case class FailureList(failures: List[Failure])

  /** The place where the results are accumulated.
    */
  final case class CombinableBox[B, C <: HList](rhs: Result[B :+: C]) {
    def :&:[A](lhs: Boxable[A]): Result[A :+: B :+: C] = this.:&:(lhs.asBox)
    def :&:[A](lhs: Box[A]): Result[A :+: B :+: C] =
      (lhs, rhs) match {
        case (failure: EmptyBox, Left(failures)) => Left(failure :: failures)
        case (failure: EmptyBox, _)              => Left(failure :: Nil)
        case (_, Left(failures))                 => Left(failures)
        case (Full(success), Right(successes))   => Right(success :+: successes)
      }
  }

}
