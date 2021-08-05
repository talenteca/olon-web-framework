package olon
package mapper

import org.specs2.mutable.Specification

object MyEnum extends Enumeration {
  val a = Value
  val b = Value
  val c = Value
  val d = Value
  val e = Value
}
  
class EnumObj extends LongKeyedMapper[EnumObj] with IdPK {
  def getSingleton = EnumObj

  object enum extends MappedEnum(this, MyEnum)
}

object EnumObj extends EnumObj with LongKeyedMetaMapper[EnumObj] 

class MappedEnumSpec extends Specification  {
  "MappedEnum Specification".title

  "MappedEnum" should {
    "preserve enumeration order when building display list" in {
      val v = EnumObj.create

      import MyEnum._
      v.enum.buildDisplayList must_== List(a.id -> a.toString, b.id -> b.toString, c.id -> c.toString, d.id -> d.toString, e.id -> e.toString)
    }
  }
}

