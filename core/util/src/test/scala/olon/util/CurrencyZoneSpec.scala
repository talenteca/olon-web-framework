package olon
package util

import org.specs2.mutable.Specification


/**
 * Systems under specification for CurrencyZone.
 */
class CurrencyZoneSpec extends Specification  {
  "CurrencyZone Specification".title

  "Australian money" should {

    "not equal to other money" in {
      val auBoolean = AU(4.42) != US(4.42)
      auBoolean mustEqual true
    }

    "be not equal to a different amount of its own money" in {
      val auBoolean = AU(4.48) == AU(4.481)
      auBoolean mustEqual false
    }


    "be equal to the same amount of its own money" in {
      val auBoolean = AU(4.42) == AU(4.42)
      auBoolean mustEqual true
    }

    "be comparable not gt" in {
      val auBoolean = AU(4.42) > AU(4.420000)
      auBoolean mustEqual false
    }

    "be creatable" in {
      AU(20.1).get must beMatching ("20.10")
    }

    "be addable" in {
      val au = AU(20.68) + AU(3.08)
      au.get must beMatching ("23.76")
    }

    "be subtractable" in {
      val au = AU(23.76) - AU(3.08)
      au.get must beMatching ("20.68")
    }

    "be mutipliable" in {
      val au = AU(20.68) * 3
      au.get must beMatching ("62.04")
    }

    "be divisable" in {
      val au = AU(20.68) / AU(3)
      au.get must beMatching ("6.89")
    }


    "be comparable gt" in {
      val auBoolean = AU(20.68) > AU(3)
      auBoolean mustEqual true
    }

    "be comparable lt" in {
      val auBoolean = AU(3.439) < AU(3.44)
      auBoolean mustEqual true
    }

    "be comparable lt or eq" in {
      val auBoolean = AU(20.68) <= AU(20.68)
      auBoolean mustEqual true
    }
  }

}

