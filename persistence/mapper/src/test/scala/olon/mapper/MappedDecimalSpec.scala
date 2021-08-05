package olon
package mapper

import org.specs2.mutable.Specification

import common._


/**
 * Systems under specification for MappedDate.
 */
class MappedDecimalSpec extends Specification  {
  "MappedDecimal Specification".title
  sequential

  val provider = DbProviders.H2MemoryProvider

  private def ignoreLogger(f: => AnyRef): Unit = ()
  def setupDB: Unit = {
    provider.setupDB
    Schemifier.destroyTables_!!(ignoreLogger _,  Dog, User)
    Schemifier.schemify(true, ignoreLogger _, Dog, User)
  }

  "MappedDecimal" should {
    "not be marked dirty on read" in {
      setupDB
      val charlie = Dog.create
      charlie.price(42.42).save

      val read = Dog.find(charlie.id)
      read.map(_.dirty_?) must_== Full(false)
    }

    "be marked dirty on update" in {
      setupDB
      val charlie = Dog.create
      charlie.price(42.42).save

      val read = Dog.find(charlie.id).openOrThrowException("This is a test")
      read.dirty_? must_== false
      read.price(100.42)
      read.price(100.42)
      read.dirty_? must_== true
    }
  }
}

