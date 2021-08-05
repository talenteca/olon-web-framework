package olon
package http

import org.specs2.mutable.Specification

/**
 * System under specification for ResourceServer.
 */
class ResourceServerSpec extends Specification  {
  "ResourceServer Specification".title

  "ResourceServer.pathRewriter" should {
    "not default jquery.js to jquery-1.3.2" in {
      ResourceServer.pathRewriter("jquery.js"::Nil) must_== List("jquery.js")
    }

    "default json to json2 minified version" in {
      (ResourceServer.pathRewriter("json.js"::Nil) must_== List("json2-min.js")) and
      (ResourceServer.pathRewriter("json2.js"::Nil) must_== List("json2-min.js"))
    }.pendingUntilFixed
  }
}
