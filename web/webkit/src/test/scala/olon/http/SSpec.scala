package olon
package http

import org.specs2.matcher.XmlMatchers
import org.specs2.mutable.Specification
import util.Helpers
import util.Props.RunModes
import LiftRules.defaultFuncNameGenerator

class SSpec extends Specification with XmlMatchers {
  "S Specification".title

  sequential

  "formFuncName" should {
    "generate random names when not in Test mode" in {
      for (mode <- RunModes.values if mode != RunModes.Test) {
        val a,b = defaultFuncNameGenerator(mode)()
        a must startWith("F")
        a.length must_== Helpers.nextFuncName.length
        a must_!= b
      }

      success
    }

    "generate predictable names in Test mode" in {
      val a,b = S.formFuncName
      a must startWith("f")
      a.length must_!= Helpers.nextFuncName.length
      a must_== b
      a must_!= S.formFuncName
    }

    "generate resort back to random names when test func-names disabled" in {
      S.disableTestFuncNames {
        val a,b = S.formFuncName
        a must startWith("F")
        a.length must_== Helpers.nextFuncName.length
        a must_!= b
      }
    }

  }
}
