package olon
package mapper

import org.specs2.mutable.Specification

import common._


/**
 * Systems under specification for MappedDate.
 */
class MappedDateSpec extends Specification  {
  "MappedDate Specification".title

  "MappedDate" should {
    "handle a Number in setFromAny" in {
      val dog = Dog2.create
      val currentDate = new java.util.Date()
      dog.createdTime.setFromAny(BigInt(currentDate.getTime))
      dog.createdTime.get mustEqual currentDate
    }

    "handle a full Box in setFromAny" in {
      val dog = Dog2.create
      val someDate = new java.util.Date(1000)
      dog.createdTime.setFromAny(Full(someDate))
      dog.createdTime.get mustEqual someDate
    }

    "handle en empty Box in setFromAny" in {
      val dog = Dog2.create
      dog.createdTime.setFromAny(Empty)
      dog.createdTime.get must beNull
    }
  }
}

