package olon.util

/** This trait marks something that can be promoted into a String. The companion
  * object has helpful conversions from Int, Symbol, Long, and Boolean
  */
trait StringPromotable

object StringPromotable {
  implicit def jsCmdToStrPromo(in: ToJsCmd): StringPromotable =
    new StringPromotable {
      override val toString = in.toJsCmd
    }

  // SCALA3 Using `?` instead of `_`
  implicit def jsCmdToStrPromo(in: (?, ToJsCmd)): StringPromotable =
    new StringPromotable {
      override val toString = in._2.toJsCmd
    }

  implicit def intToStrPromo(in: Int): StringPromotable =
    new StringPromotable {
      override val toString = in.toString
    }

  implicit def symbolToStrPromo(in: Symbol): StringPromotable =
    new StringPromotable {
      override val toString = in.name
    }

  implicit def longToStrPromo(in: Long): StringPromotable =
    new StringPromotable {
      override val toString = in.toString
    }

  implicit def booleanToStrPromo(in: Boolean): StringPromotable =
    new StringPromotable {
      override val toString = in.toString
    }
}
