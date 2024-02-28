package olon
package util

import scala.xml.NodeSeq
import scala.xml.Text

import common._

sealed trait IterableFunc extends Function1[NodeSeq, Seq[NodeSeq]] {
  def apply(ns: NodeSeq): Seq[NodeSeq]
}

object IterableFunc {
  implicit def itNodeSeq[C](
      it: NodeSeq => C
  )(implicit ev: C => Iterable[NodeSeq]): IterableFunc =
    new IterableFunc {
      def apply(in: NodeSeq): Seq[NodeSeq] = ev(it(in)).toSeq
    }

  implicit def itNodeSeqPromotable(it: NodeSeq => NodeSeq): IterableFunc =
    new IterableFunc {
      def apply(in: NodeSeq): Seq[NodeSeq] = List(it(in))
    }

  implicit def itStringFuncPromotable(it: NodeSeq => String): IterableFunc =
    new IterableFunc {
      def apply(in: NodeSeq): Seq[NodeSeq] = it(in) match {
        case null => List(NodeSeq.Empty)
        case str  => List(Text(str))
      }
    }

  implicit def itStringPromotable(it: NodeSeq => Seq[String]): IterableFunc =
    new IterableFunc {
      def apply(in: NodeSeq): Seq[NodeSeq] =
        it(in).filter(_ ne null).map(a => Text(a))
    }

  implicit def boxStringPromotable(it: NodeSeq => Box[String]): IterableFunc =
    new IterableFunc {
      def apply(in: NodeSeq): Seq[NodeSeq] =
        it(in).filter(_ ne null).toList.map(a => Text(a))
    }

  implicit def optionStringPromotable(
      it: NodeSeq => Option[String]
  ): IterableFunc =
    new IterableFunc {
      def apply(in: NodeSeq): Seq[NodeSeq] =
        it(in).filter(_ ne null).toList.map(a => Text(a))
    }
}
