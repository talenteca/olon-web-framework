package olon
package sitemap

import org.specs2.mutable.Specification

/** Systems under specification for Menu DSL.
  */
class MenuDslSpec extends Specification {
  "Menu DSL Specification".title

  "The Menu DSL" should {
    "allow basic menu definition via '/ path'" in {
      val menu = (Menu("Test") / "foo").toMenu
      menu.loc.link.uriList mustEqual List("foo")
      menu.loc.link.matchHead_? mustEqual false
    }

    "allow wildcard menu definitions via '/ path / **'" in {
      val menu = (Menu("Test") / "foo" / **).toMenu
      menu.loc.link.uriList mustEqual List("foo")
      menu.loc.link.matchHead_? mustEqual true
    }

    "handle LocParams" in {
      import Loc._
      val worthlessTest =
        If(() => System.currentTimeMillis % 2 == 0, "So sad for you!")

      val menu1 = Menu("Test") / "foo" >> worthlessTest
      val menu2 = Menu("Test") / "foo" rule worthlessTest

      // Got a weird type error when trying to just use "must contain" :(
      menu1.toMenu.loc.params.exists(_ == worthlessTest) mustEqual true
      menu2.toMenu.loc.params.exists(_ == worthlessTest) mustEqual true
    }

    "handle submenus" in {
      val menu =
        Menu("Foo") / "test" submenus (
          Menu("Bar") / "bar",
          Menu("Bat") / "bat"
        )

      menu.toMenu.kids.size mustEqual 2
    }

    "handle sub-submenus" in {
      val menu =
        Menu("Foo") / "test" submenus (
          Menu("Bar") / "bar" submenus (
            Menu("BarOne") / "bar" / "one",
            Menu("BarTwo") / "bar" / "two"
          ),
          Menu("Bat") / "bat"
        )

      menu.toMenu.kids(0).kids.size mustEqual 2
    }

    "handle I18N menu names" in {
      val menu = Menu.i("Home") / "index"

      menu.toMenu.loc.name mustEqual "Home"
    }
  }

  "MenuItems" should {
    "support nesting deeper than two levels" in {
      val menu =
        Menu("Foo") / "test" submenus (
          Menu("Bar") / "bar" submenus (
            Menu("BarOne") / "bar" / "one",
            Menu("BarTwo") / "bar" / "two",
            Menu("BarThree") / "bar" / "three"
          ),
          Menu("Bat") / "bat"
        )

      val complete = SiteMap(menu)
        .kids(0)
        .makeMenuItem(List())
        .openOrThrowException("legacy code")

      complete.kids.size must_== 2
      complete.kids(0).kids.size must_== 3
    }
  }
}
