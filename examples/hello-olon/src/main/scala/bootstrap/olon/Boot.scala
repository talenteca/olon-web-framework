package bootstrap.olon

import olon.common.Full
import olon.http.Html5Properties
import olon.http.js.jquery.JQueryArtifacts
import olon.http.LiftRules
import olon.http.Req
import olon.javascript.JavaScriptContext
import olon.sitemap.Menu
import olon.sitemap.SiteMap

class Boot {

  def boot = {

    LiftRules.addToPackages("hello")

    def sitemap = SiteMap(Menu.i("Home") / "index")

    LiftRules.setSiteMapFunc(() => sitemap)

    LiftRules.jsArtifacts = JQueryArtifacts

    LiftRules.ajaxStart = Full(() => LiftRules.jsArtifacts.show("ajax-loader").cmd)

    LiftRules.ajaxEnd = Full(() => LiftRules.jsArtifacts.hide("ajax-loader").cmd)

    LiftRules.early.append(_.setCharacterEncoding("UTF-8"))

    LiftRules.htmlProperties.default.set((r: Req) => new Html5Properties(r.userAgent))
    JavaScriptContext.install()

    LiftRules.supplementalHeaders.default.set(
      List(
        ("Content-Security-Policy", "frame-ancestors 'self' http://localhost:8080"),
        ("Content-Language", "en")
      )
    )


  }
}
