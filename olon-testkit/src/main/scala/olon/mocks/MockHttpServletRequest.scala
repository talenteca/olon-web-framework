package olon
package mocks

import jakarta.servlet._
import jakarta.servlet.http._

import java.io.BufferedReader
import java.io.ByteArrayInputStream
import java.io.InputStreamReader
import java.net.URI
import java.net.URL
import java.security.Principal
import java.text.ParseException
import java.util.Collection
import java.util.Locale
import java.util.{Enumeration => JEnum}
import scala.collection.mutable.ListBuffer
import scala.jdk.CollectionConverters._
import scala.xml.NodeSeq

import common.{Box, Empty}
import util.Helpers
import json.JsonAST._

/** A Mock ServletRequest. Change its state to create the request you are
  * interested in. At the very least, you will need to change method and path.
  *
  * There are several things that aren't supported:
  *
  * <ul> <li>getRequestDispatcher - returns null always</li>
  * <li>getRequestedSessionId - always returns null. The related
  * isRequestedSessionId... methods similarly all return false</li>
  * <li>getRealPath - simply returns the input string</li> </ul>
  *
  * @author
  *   Steve Jenson (stevej@pobox.com)
  * @author
  *   Derek Chen-Becker
  *
  * @param url
  *   The URL to extract from
  * @param contextPath
  *   The context path for this request. Defaults to "" per the Servlet API.
  */
class MockHttpServletRequest(
    val url: String = null,
    var contextPath: String = ""
) extends HttpServletRequest {

  def getProtocolRequestId(): String = ???

  def getRequestId(): String = ???

  def getServletConnection(): jakarta.servlet.ServletConnection = ???

  var attributes: Map[String, Object] = Map()

  var authType: String = null

  /** The character encoding of the request.
    *
    * Defaults to UTF-8. Note that this differs from the default encoding per
    * the HTTP spec (ISO-8859-1), so you will need to change this if you need
    * something other than UTF-8.
    */
  var charEncoding: String = "UTF-8" // HTTP's default encoding

  // SCALA3 creating an internal alias for `body` for external usage of `body =`
  /** The raw body of the request. */
  private var _body: Array[Byte] = Array()
  def body: Array[Byte] = _body

  /** Sets the body to the given string. The content type is set to
    * "text/plain".
    *
    * Note that the String will be converted to bytes based on the current
    * setting of charEncoding.
    */
  def body_=(s: String): Unit = body_=(s, "text/plain")

  /** Sets the body to the given string and content type.
    *
    * Note that the String will be converted to bytes based on the current
    * setting of charEncoding.
    */
  def body_=(s: String, contentType: String): Unit = {
    _body = s.getBytes(charEncoding)
    this.contentType = contentType
  }

  /** Sets the body to the given elements. Also sets the contentType to
    * "text/xml"
    *
    * Note that the elements will be converted to bytes based on the current
    * setting of charEncoding.
    */
  def body_=(nodes: NodeSeq): Unit = body_=(nodes, "text/xml")

  /** Sets the body to the given elements and content type.
    *
    * Note that the elements will be converted to bytes based on the current
    * setting of charEncoding.
    */
  def body_=(nodes: NodeSeq, contentType: String): Unit = {
    _body = nodes.toString.getBytes(charEncoding)
    this.contentType = contentType
  }

  /** Sets the body to the given json value. Also sets the contentType to
    * "application/json"
    */
  // SCALA3 adding JValue generic parameter type
  def body_=(jval: JValue[?]): Unit = body_=(jval, "application/json")

  /** Sets the body to the given json value and content type.
    */
  // SCALA3 adding JValue generic parameter type
  def body_=(jval: JValue[?], contentType: String): Unit = {
    import json.JsonAST

    _body = JsonAST.prettyRender(jval).getBytes(charEncoding)
    this.contentType = contentType
  }

  var contentType: String = null

  var cookies: List[Cookie] = Nil

  var headers: Map[String, List[String]] = Map()

  /** The port that this request was received on. You should probably change
    * serverPort as well if you change this.
    */
  var localPort = 80

  /** The local address that the request was received on.
    *
    * If you change this you should probably change localName and serverName as
    * well.
    */
  var localAddr: String = "127.0.0.1"

  /** The local hostname that the request was received on.
    *
    * If you change this you should probably change localAddr and serverName as
    * well.
    */
  var localName: String = "localhost"

  /** The preferred locales for the client, in decreasing order of preference.
    * If not set, the default locale will be used.
    */
  var locales: List[Locale] = Nil

  var method: String = "GET"

  /** The query parameters for the request. There are two main ways to set this
    * List, either by modifying the parameters var directly, or by assigning to
    * queryString, which will parse the provided string into GET parameters.
    */
  var parameters: List[(String, String)] = Nil

  var path: String = "/"

  var pathInfo: String = null

  var protocol = "HTTP/1.0"

  def queryString: String =
    if (method == "GET" && !parameters.isEmpty) {
      parameters.map { case (k, v) => k + "=" + v }.mkString("&")
    } else {
      null
    }

  def queryString_=(q: String): Unit = {
    if (q != null && q.length > 0) {
      val newParams = ListBuffer[(String, String)]()

      q.split('&').foreach { pair =>
        pair.split('=') match {
          case Array(key, value) => {
            // Append to the current key's value
            newParams += key -> value
          }
          case Array("") =>
            throw new IllegalArgumentException(
              "Invalid query string: \"" + q + "\""
            )
          case Array(key) => {
            // Append to the current key's value
            newParams += key -> ""
          }
          case _ =>
            throw new IllegalArgumentException(
              "Invalid query string: \"" + q + "\""
            )
        }
      }

      parameters = newParams.toList
      method = "GET"
    }
  }

  var remotePort = 80

  /** The hostname of the client that sent the request.
    *
    * If you change this you should probably change remoteAddr as well.
    */
  var remoteHost: String = null

  /** The address of the client that sent the request.
    *
    * If you change this you should probably change remoteHost as well.
    */
  var remoteAddr: String = null

  // Default to the root URI
  var requestUri: String = "/"

  var user: String = null

  var userRoles: Set[String] = Set()

  var userPrincipal: Principal = null

  var scheme = "http"

  /** Indicates whether the request is being handled by a secure protocol (e.g.
    * HTTPS). If you set the scheme to https you should set this to true.
    */
  var secure = false

  var serverName: String = "localhost"

  /** The port that this request was received on. You should probably change
    * localPort as well if you change this.
    */
  var serverPort = 80

  // Defaults to "" for servlet matching "/*"
  var servletPath: String = ""

  var session: HttpSession = null

  // BEGIN PRIMARY CONSTRUCTOR LOGIC
  if (
    contextPath.length > 0 && (contextPath(0) != '/' || contextPath.last == '/')
  ) {
    throw new IllegalArgumentException(
      "Context path must be empty, or must start with a '/' and not end with a '/': " + contextPath
    )
  }

  if (url != null) {
    processUrl(url)
  }

  // END PRIMARY CONSTRUCTOR

  /** Construct a new mock request for the given URL. See processUrl for
    * limitations.
    *
    * @param url
    *   The URL to extract from
    */
  def this(url: URL) = {
    this()
    processUrl(url)
  }

  /** Construct a new mock request for the given URL. See processUrl for
    * limitations.
    *
    * @param url
    *   The URL to extract from
    * @param contextPath
    *   The servlet context of the request.
    */
  def this(url: URL, contextPath: String) = {
    this(null: String, contextPath)
    processUrl(url)
  }

  /** Set fields based on the given url string. If the url begins with "http" it
    * is assumed to be a full URL, and is processed with processUrl(URL). If the
    * url begins with "/" then it's assumed to be only the path and query
    * string.
    *
    * @param url
    *   The URL to extract from
    */
  def processUrl(url: String): Unit = {
    if (url.toLowerCase.startsWith("http")) {
      processUrl(URI.create(url).parseServerAuthority().toURL())
    } else if (url.startsWith("/")) {
      computeRealPath(url).split('?') match {
        case Array(path, query) => this.path = path; queryString = query
        case Array(path)        => this.path = path; queryString = null
        case _ =>
          throw new IllegalArgumentException("too many '?' in URL : " + url)
      }
    } else {
      throw new IllegalArgumentException(
        "Could not process url: \"%s\"".format(url)
      )
    }
  }

  /** Set fields based on the given URL. There are several limitations:
    *
    * <ol> <li>The host portion is used to set localAddr, localHost and
    * serverName. You will need to manually set these if you want different
    * behavior.</li> <li>The userinfo field isn't processed. If you want to mock
    * BASIC authentication, use the addBasicAuth method</li> </ol>
    *
    * @param url
    *   The URL to extract from
    * @param contextPath
    *   The servlet context of the request. Defaults to ""
    */
  def processUrl(url: URL): Unit = {
    // Deconstruct the URL to set values
    url.getProtocol match {
      case "http"  => scheme = "http"; secure = false
      case "https" => scheme = "https"; secure = true
      case other =>
        throw new IllegalArgumentException("Unsupported protocol: " + other)
    }

    localName = url.getHost
    localAddr = localName
    serverName = localName

    if (url.getPort == -1) {
      localPort = 80
    } else {
      localPort = url.getPort
    }

    serverPort = localPort

    path = computeRealPath(url.getPath)

    queryString = url.getQuery

  }

  /** Compute the path portion after the contextPath */
  def computeRealPath(path: String) = {
    if (!path.startsWith(contextPath)) {
      throw new IllegalArgumentException(
        "Path \"%s\" doesn't begin with context path \"%s\"!".format(
          path,
          contextPath
        )
      )
    }

    path.substring(contextPath.length)
  }

  /** Adds an "Authorization" header, per RFC1945.
    */
  def addBasicAuth(user: String, pass: String): Unit = {
    val hashedCredentials =
      Helpers.base64Encode((user + ":" + pass).getBytes)
    headers += "Authorization" -> List("Basic " + hashedCredentials)
  }

  // ServletRequest methods

  def getAttribute(key: String): Object = attributes.get(key).getOrElse(null)

  def getAttributeNames(): JEnum[String] =
    attributes.keys.iterator.asJavaEnumeration

  def getCharacterEncoding(): String = charEncoding

  def getContentLength(): Int = _body.length

  def getContentType(): String = contentType

  def getInputStream(): ServletInputStream = {
    new MockServletInputStream(new ByteArrayInputStream(_body))
  }

  def getLocalAddr(): String = localAddr

  def getLocale(): Locale = locales.headOption.getOrElse(Locale.getDefault)

  def getLocales(): JEnum[Locale] = locales.iterator.asJavaEnumeration

  def getLocalName(): String = localName

  def getLocalPort(): Int = localPort

  def getParameter(key: String): String =
    parameters.find(_._1 == key).map(_._2) getOrElse null

  def getParameterMap(): java.util.Map[String, Array[String]] = {
    // Build a new map based on the parameters List
    var newMap = Map[String, List[String]]().withDefault(_ => Nil)

    parameters.foreach { case (k, v) =>
      newMap += k -> (newMap(
        k
      ) ::: v :: Nil) // Ugly, but it works and keeps order
    }

    newMap
      .map { case (k, v) => (k, v.toArray) }
      .asInstanceOf[Map[String, Array[String]]]
      .asJava
//    asMap(newMap.map{case (k,v) => (k,v.toArray)}.asInstanceOf[Map[Object,Object]])
  }

  def getParameterNames(): JEnum[String] =
    parameters.map(_._1).distinct.iterator.asJavaEnumeration

  def getParameterValues(key: String): Array[String] =
    parameters.filter(_._1 == key).map(_._2).toArray

  def getProtocol(): String = protocol

  def getReader(): BufferedReader =
    new BufferedReader(
      new InputStreamReader(new ByteArrayInputStream(_body), charEncoding)
    )

  def getRealPath(s: String): String = s

  def getRemoteAddr(): String = remoteAddr

  def getRemoteHost(): String = remoteHost

  def getRemotePort(): Int = remotePort

  def getRequestDispatcher(s: String): RequestDispatcher = null

  def getScheme(): String = scheme

  def getServerName(): String = serverName

  def getServerPort(): Int = serverPort

  def isSecure = secure

  def removeAttribute(key: String): Unit = attributes -= key

  def setAttribute(key: String, value: Object): Unit =
    attributes += (key -> value)

  def setCharacterEncoding(enc: String): Unit = charEncoding = enc

  // HttpServletRequest methods
  def getAuthType(): String = authType

  def getContextPath(): String = contextPath

  def getCookies(): Array[Cookie] = cookies.toArray

  def getDateHeader(h: String): Long = {
    val handler: PartialFunction[Throwable, Box[Long]] = {
      case pe: ParseException => {
        throw new IllegalArgumentException(
          "Could not parse the date for %s : \"%s\"".format(h, getHeader(h), pe)
        )
        Empty
      }
    }

    Helpers
      .tryo(
        handler, {
          // Have to use internetDateFormatter directly since parseInternetDate returns the epoch date on failure
          Box
            .!!(getHeader(h))
            .map(Helpers.internetDateFormatter.parse(_).getTime)
        }
      )
      .flatMap(x => x)
      .openOr(-1L)
  }

  def getHeader(h: String): String = headers.get(h) match {
    case Some(v :: _) => v
    case _            => null
  }

  def getHeaderNames(): JEnum[String] = headers.keys.iterator.asJavaEnumeration

  def getHeaders(s: String): JEnum[String] =
    headers.getOrElse(s, Nil).iterator.asJavaEnumeration

  def getIntHeader(h: String): Int = {
    Box.!!(getHeader(h)).map(_.toInt).openOr(-1)
  }

  def getMethod(): String = method

  def getPathInfo(): String = pathInfo

  def getPathTranslated(): String = path

  def getQueryString(): String = queryString

  def getRemoteUser(): String = user

  def getRequestedSessionId(): String = null

  def getRequestURI(): String = contextPath + path

  def getRequestURL(): StringBuffer = {
    val buffer = new StringBuffer(scheme + "://" + localName)

    if (localPort != 80) buffer.append(":" + localPort)

    if (contextPath != "") buffer.append(contextPath)

    buffer.append(path)

    if (queryString ne null) {
      buffer.append("?" + queryString)
    }

    buffer
  }

  def getServletPath(): String = servletPath

  def getSession(): HttpSession = getSession(true)

  def getSession(create: Boolean): HttpSession = {
    if ((session eq null) && create) {
      session = new MockHttpSession
    }
    session
  }

  def getUserPrincipal(): java.security.Principal = null

  def isRequestedSessionIdFromURL(): Boolean = false

  def isRequestedSessionIdFromUrl(): Boolean = false

  def isRequestedSessionIdFromCookie(): Boolean = false

  def isRequestedSessionIdValid(): Boolean = false

  def isUserInRole(user: String): Boolean = false

  /** A utility method to set the given header to an RFC1123 date based on the
    * given long value (epoch seconds).
    */
  def setDateHeader(s: String, l: Long): Unit = {
    headers += (s -> List(Helpers.toInternetDate(l)))
  }

  def getParts(): Collection[Part] = {
    Seq[Part]().asJava
  }

  def getPart(partName: String): Part = {
    null
  }

  def login(username: String, password: String): Unit = ()

  def logout(): Unit = ()

  def authenticate(resp: HttpServletResponse) = true

  def getAsyncContext(): AsyncContext = null
  def getDispatcherType(): DispatcherType = null
  def getServletContext(): ServletContext = null
  def isAsyncStarted(): Boolean = false
  def isAsyncSupported(): Boolean = false
  def startAsync(
      request: jakarta.servlet.ServletRequest,
      response: jakarta.servlet.ServletResponse
  ): AsyncContext = null
  def startAsync(): AsyncContext = null
  def changeSessionId(): String = null
  def getContentLengthLong(): Long = _body.length

  def upgrade[T <: jakarta.servlet.http.HttpUpgradeHandler](x$1: Class[T]): T =
    ???
}
