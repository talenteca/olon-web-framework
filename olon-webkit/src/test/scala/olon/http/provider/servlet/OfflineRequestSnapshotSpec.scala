package olon.http.provider.servlet

import olon.http.provider._
import olon.mockweb.WebSpec

object OfflineRequestSnapshotSpec extends WebSpec {

  // SCALA3 Using `private` instead of `private[this]`
  private val X_SSL = "X-SSL"
  private val xSSLHeader = HTTPParam(X_SSL, List("true")) :: Nil

  "OfflineRequestSnapshot" should {
    "have a 'headers' method that returns the list of headers with a given name" in {
      val req = getRequestSnapshot(originalPort = 80, headers = xSSLHeader)
      req.headers("X-SSL") shouldEqual List("true")
      req.headers("Unknown") must beEmpty
    }

    "have the serverPort value" in {
      "443 when the 'X-SSL' header is set to the string 'true' (case-insensitive) and original port is 80" in {
        val port80Req =
          getRequestSnapshot(originalPort = 80, headers = xSSLHeader)
        port80Req.serverPort shouldEqual 443
      }

      s"equal to the original request-port when" in {
        s"the '$X_SSL' header is absent" in {
          val nonSSLReq = getRequestSnapshot(originalPort = 80)
          nonSSLReq.serverPort shouldEqual 80
        }

        s"the '$X_SSL' header is not set to the string 'true' (case-insensitive)" in {
          val falseSSLHeaderReq = getRequestSnapshot(
            originalPort = 90,
            headers = HTTPParam(X_SSL, List("anything")) :: Nil
          )
          falseSSLHeaderReq.serverPort shouldEqual 90
        }

        "the original request-port is not 80" in {
          val req = getRequestSnapshot(originalPort = 90, headers = xSSLHeader)
          req.serverPort shouldEqual 90
        }
      }
    }

    "have a 'param' method that returns the list of parameters with a given name (case-sensitive)" in {
      val tennisParams = List("Roger Federer", "Raphael Nadal")
      val swimmingParams = List("Michael Phelps", "Ian Thorpe")
      val params = HTTPParam("tennis", tennisParams) :: HTTPParam(
        "swimming",
        swimmingParams
      ) :: Nil
      val snapshot = getRequestSnapshot(80, params = params)

      snapshot.param("tennis") shouldEqual tennisParams
      snapshot.param("Tennis") should beEmpty
      snapshot.param("swimming") shouldEqual swimmingParams
    }
  }

  // SCALA3 Using `private` instead of `private[this]`
  private def getRequestSnapshot(
      originalPort: Int,
      headers: List[HTTPParam] = Nil,
      params: List[HTTPParam] = Nil
  ) = {
    val mockHttpRequest = mock[HTTPRequest]()
    val httpProvider = new HTTPProvider {
      override protected def context: HTTPContext = null
    }

    when(mockHttpRequest.headers).thenReturn(headers)
    when(mockHttpRequest.cookies).thenReturn(Nil)
    when(mockHttpRequest.params).thenReturn(params)
    when(mockHttpRequest.serverPort).thenReturn(originalPort)
    new OfflineRequestSnapshot(mockHttpRequest, httpProvider)
  }

}
