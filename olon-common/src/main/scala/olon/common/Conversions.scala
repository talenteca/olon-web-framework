package olon
package common

import scala.xml._

/*
 * This file contains common conversions and other utilities to make
 * conversions that are common
 */

/** This trait is used to unify `String`s and `[[scala.xml.NodeSeq NodeSeq]]`s
  * into one type. It is used in conjuction with the implicit conversions
  * defined in its [[StringOrNodeSeq$ companion object]].
  */
sealed trait StringOrNodeSeq {
  def nodeSeq: scala.xml.NodeSeq
}

/** Provides implicit conversions to the `StringOrNodeSeq` trait, which can in
  * turn be implicitly converted to `[[scala.xml.NodeSeq NodeSeq]]`. This allows
  * using a `String` as a natural part of `NodeSeq` APIs without having to
  * explicitly wrap it in `scala.xml.Text` or having to write overloads for all
  * methods that should accept both.
  *
  * This is used in certain Lift APIs, for example, to accept either a `String`
  * or more complex content. For example, a `button` can have either a simple
  * label or complex HTML content. HTML APIs that can do this can accept a
  * parameter of type `StringOrNodeSeq` to allow the user to pass either in as
  * their needs dictate.
  */
object StringOrNodeSeq {
  implicit def strTo[T](str: T)(implicit ev: T => String): StringOrNodeSeq =
    new StringOrNodeSeq {
      def nodeSeq: NodeSeq = Text(ev(str))
    }

  /** This is written in terms of a `Seq[Node]` to make sure Scala converts
    * everything it should to a `StringOrNodeSeq`. `NodeSeq` is a `Seq[Node]`.`
    */
  implicit def nsTo(ns: Seq[Node]): StringOrNodeSeq =
    new StringOrNodeSeq {
      def nodeSeq: NodeSeq = ns
    }

  implicit def toNodeSeq(sns: StringOrNodeSeq): NodeSeq = sns.nodeSeq
}

/** This trait is used to unify `()=>String` and `String` into one type. It is
  * used in conjunction with the implicit conversions defined in its
  * [[StringFunc$ companion object]].
  */
sealed trait StringFunc {
  def func: () => String
}

/** Provides implicit conversions to the `StringFunc` trait. This allows using a
  * `String` as a natural part of APIs that want to allow the flexibility of a
  * `()=>String` without having to write overloads for all methods that should
  * accept both.
  *
  * Lift's Menu API, for example, allows CSS classes to be defined either as a
  * `String` or a `()=>String`. The latter could use the current request and
  * session state to do more interesting things than a hard-coded `String`
  * would, while the former is simpler to use.
  */
object StringFunc {

  /** Implicit conversion from any type that in turn has an implicit conversion
    * to a `String`, to a `StringFunc`. In particular, this means that if a
    * given method takes a `StringFunc` as a parameter, it can accept either a
    * `String` and any type that has an implicit conversion to `String` in
    * scope.
    */
  implicit def strToStringFunc[T](str: T)(implicit f: T => String): StringFunc =
    ConstStringFunc(f(str))

  /** Implicit conversion from any function that produces a type that in turn
    * has an implicit conversion to a `String`, to a `StringFunc`. In
    * particular, this means that if a given method takes a `StringFunc` as a
    * parameter, it can accept either a function that returns a `String` and a
    * function that returns any other type that has an implicit conversion to
    * `String` in scope.
    */
  implicit def funcToStringFunc[T](func: () => T)(implicit
      f: T => String
  ): StringFunc =
    RealStringFunc(() => f(func()))
}

/** See `[[StringFunc]]`.
  */
final case class RealStringFunc(func: () => String) extends StringFunc

/** See `[[StringFunc]]`.
  */
final case class ConstStringFunc(str: String) extends StringFunc {
  lazy val func = () => str
}
