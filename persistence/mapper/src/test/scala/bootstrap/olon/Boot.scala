package bootstrap.olon

import olon.http._
import olon.sitemap._

/**
  * A class that's instantiated early and run.  It allows the application
  * to modify lift's environment
  */
class Boot {
  def boot(): Unit = {
    // where to search snippet
    LiftRules.addToPackages("olon.webapptest")

    // Build SiteMap
    val entries = Menu("Home") / "index" ::
    Menu("htmlFragmentWithHead") / "htmlFragmentWithHead" ::
    Menu("htmlSnippetWithHead") / "htmlSnippetWithHead" ::
    Nil

    LiftRules.setSiteMap(SiteMap(entries:_*))
  }
}
