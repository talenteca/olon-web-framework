package olon.sitemap

import olon.http.LiftRules
import olon.http.S
import olon.mockweb.WebSpec

import scala.xml.Elem
import scala.xml.NodeSeq

// SCALA3 Removing `_` for passing function as a value
object FlexMenuBuilderSpec extends WebSpec(FlexMenuBuilderSpecBoot.boot) {
  "FlexMenuBuilder Specification".title

  val html1 = <div data-lift="MenuBuilder.builder?group=hometabsv2"></div>

  "FlexMenuBuilder" should {
    val testUrl = "http://foo.com/help"
    val testUrlPath = "http://foo.com/index1"

    "Link to Self".withSFor(testUrl) in {
      object MenuBuilder extends FlexMenuBuilder {
        override def linkToSelf = true
      }
      val linkToSelf =
        <ul><li><a href="/index">Home</a></li><li><a href="/help">Help</a></li><li><a href="/help2">Help2</a></li></ul>
      val actual = MenuBuilder.render
      linkToSelf must beEqualToIgnoringSpace(actual)
    }

    "expandAll".withSFor(testUrl) in {
      object MenuBuilder extends FlexMenuBuilder {
        override def expandAll = true
      }
      val expandAll: NodeSeq =
        <ul><li><a href="/index">Home</a></li><li><span>Help</span><ul><li><a href="/index1">Home1</a></li><li><a href="/index2">Home2</a></li></ul></li><li><a href="/help2">Help2</a><ul><li><a href="/index3">Home3</a></li><li><a href="/index4">Home4</a></li></ul></li></ul>
      val actual = MenuBuilder.render
      expandAll.toString must_== actual.toString
    }

    "Add css class to item in the path".withSFor(testUrlPath) in {
      object MenuBuilder extends FlexMenuBuilder {
        override def updateForPath(nodes: Elem, path: Boolean): Elem = {
          if (path) {
            nodes % S.mapToAttrs(Map("class" -> "active"))
          } else {
            nodes
          }
        }
      }
      val itemInPath: NodeSeq =
        <ul><li><a href="/index">Home</a></li><li class="active"><a href="/help">Help</a><ul><li class="active"><span>Home1</span></li><li><a href="/index2">Home2</a></li></ul></li><li><a href="/help2">Help2</a></li></ul>
      val actual = MenuBuilder.render
      itemInPath.toString must_== actual.toString
    }

    "Add css class to the current item".withSFor(testUrl) in {
      object MenuBuilder extends FlexMenuBuilder {
        override def updateForCurrent(nodes: Elem, current: Boolean): Elem = {
          if (current) {
            nodes % S.mapToAttrs(Map("class" -> "active"))
          } else {
            nodes
          }
        }
      }
      val itemInPath: NodeSeq =
        <ul><li><a href="/index">Home</a></li><li class="active"><span>Help</span></li><li><a href="/help2">Help2</a></li></ul>
      val actual = MenuBuilder.render
      itemInPath.toString must_== actual.toString
    }
  }

}

/** This only exists to keep the WebSpecSpec clean. Normally, you could just use
  * "() => bootstrap.Boot.boot".
  */
object FlexMenuBuilderSpecBoot {
  def boot(): Unit = {
    def siteMap = SiteMap(
      Menu.i("Home") / "index",
      (Menu.i("Help") / "help").submenus(
        Menu.i("Home1") / "index1",
        Menu.i("Home2") / "index2"
      ),
      (Menu.i("Help2") / "help2").submenus(
        Menu.i("Home3") / "index3",
        Menu.i("Home4") / "index4"
      )
    )
    LiftRules.setSiteMap(siteMap)
  }
}
