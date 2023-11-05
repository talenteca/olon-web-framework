package olon.mocks

import jakarta.servlet._
import jakarta.servlet.http._
import olon.common.Logger

import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.nio.file.Files
import java.nio.file.Paths
import java.util.Vector
import scala.jdk.CollectionConverters._

/** An example of how to use these mock classes in your unit tests:
  *
  * def testLiftCore = { val output = new ByteArrayOutputStream val outputStream
  * \= new MockServletOutputStream(output) val writer = new
  * PrintWriter(outputStream)
  *
  * val req = new MockHttpServletRequest req.method = "GET" req.path = "/" val
  * res = new MockHttpServletResponse(writer, outputStream)
  *
  * val filter = new LiftFilter filter.init(new MockFilterConfig(new
  * MockServletContext("target/test1-1.0-SNAPSHOT"))) filter.doFilter(req,
  * res,new DoNothingFilterChain)
  * assertTrue(output.toString.startsWith("<?xml")) }
  */

/** A Mock ServletContext. LiftFilter expects a ServletContext inside a
  * FilterConfig
  *
  * @param target
  *   the target directory where your template files live
  *
  * @author
  *   Steve Jenson (stevej@pobox.com)
  */
class MockServletContext(var target: String) extends ServletContext {

  def addJspFile(
      servletName: String,
      jspFile: String
  ): ServletRegistration.Dynamic = null

  def getRequestCharacterEncoding(): String = null

  def getResponseCharacterEncoding(): String = null

  def getSessionTimeout(): Int = 0

  def setRequestCharacterEncoding(encoding: String): Unit = {}

  def setResponseCharacterEncoding(encoding: String): Unit = {}

  def setSessionTimeout(sessionTimeout: Int): Unit = {}

  def getInitParameter(name: String): String = null

  def getInitParameterNames(): java.util.Enumeration[String] =
    new Vector[String]().elements
  def getAttribute(f: String): Object = null
  def getAttributeNames(): java.util.Enumeration[String] =
    new Vector[String]().elements
  def removeAttribute(name: String): Unit = {}
  def setAttribute(name: String, o: Object): Unit = {}
  def getContext(path: String): ServletContext = this
  def getMajorVersion() = 2
  def getMimeType(file: String): String = null
  def getMinorVersion() = 3
  def getRealPath(path: String): String = null
  def getNamedDispatcher(name: String): RequestDispatcher = null
  def getRequestDispatcher(path: String): RequestDispatcher = null
  def getResource(path: String): java.net.URL = null
  def getResourceAsStream(path: String): java.io.InputStream = {
    val file = Paths.get(target + path)
    if (Files.exists(file)) {
      Files.newInputStream(file)
    } else {
      null
    }
  }

  def getResourcePaths(path: String): java.util.Set[String] = null
  def getServerInfo(): String = null
  def getServlet(name: String): Servlet = null
  def getServletContextName(): String = null
  def getServletNames(): java.util.Enumeration[String] =
    new Vector[String]().elements
  def getServlets(): java.util.Enumeration[Servlet] =
    new Vector[Servlet]().elements
  def log(msg: String, t: Throwable): Unit = {
    t.printStackTrace
    log(msg)
  }
  def log(e: Exception, msg: String): Unit = {
    e.printStackTrace
    log(msg)
  }
  def log(msg: String) = println("MockServletContext.log: " + msg)
  def getContextPath(): String = null

  def addFilter(
      filterName: String,
      filterClass: Class[_ <: Filter]
  ): FilterRegistration.Dynamic = null

  def addFilter(
      filterName: String,
      filter: Filter
  ): FilterRegistration.Dynamic = null

  def addFilter(
      filterName: String,
      className: String
  ): FilterRegistration.Dynamic = null

  def addListener(listenerClass: Class[_ <: java.util.EventListener]): Unit = ()

  def addListener[T <: java.util.EventListener](listener: T): Unit = ()

  def addListener(listenerClass: String): Unit = ()

  def addServlet(
      servletName: String,
      servletClass: Class[_ <: Servlet]
  ): ServletRegistration.Dynamic = null

  def addServlet(
      servletName: String,
      servlet: Servlet
  ): ServletRegistration.Dynamic = null

  def addServlet(
      servletName: String,
      servletClass: String
  ): ServletRegistration.Dynamic = null

  // This remains unimplemented since we can't provide a Null here due to type restrictions.

  def createFilter[T <: Filter](filter: Class[T]): T = ???

  def createListener[T <: java.util.EventListener](listener: Class[T]): T = ???

  def createServlet[T <: Servlet](servletClass: Class[T]): T =
    ???

  def getDefaultSessionTrackingModes(): java.util.Set[SessionTrackingMode] =
    Set.empty[SessionTrackingMode].asJava

  def declareRoles(roles: String*): Unit = ()

  def getClassLoader(): ClassLoader = getClass.getClassLoader

  def getEffectiveMajorVersion(): Int = 0

  def getEffectiveMinorVersion(): Int = 0

  def getEffectiveSessionTrackingModes(): java.util.Set[SessionTrackingMode] =
    null

  def getFilterRegistration(filterName: String): FilterRegistration =
    null

  def getFilterRegistrations(): java.util.Map[String, _ <: FilterRegistration] =
    null

  def getJspConfigDescriptor(): descriptor.JspConfigDescriptor =
    null

  def getServletRegistration(servletName: String): ServletRegistration =
    null

  def getServletRegistrations()
      : java.util.Map[String, _ <: ServletRegistration] = null

  def getSessionCookieConfig(): SessionCookieConfig = null

  def setInitParameter(key: String, value: String): Boolean = true

  def setSessionTrackingModes(
      trackingModes: java.util.Set[SessionTrackingMode]
  ): Unit = ()

  def getVirtualServerName(): String = null
}

/** A Mock FilterConfig. Construct with a MockServletContext and pass into
  * LiftFilter.init
  */
class MockFilterConfig(servletContext: ServletContext) extends FilterConfig {

  def getFilterName(): String = "LiftFilter" // as in lift's default web.xml

  def getInitParameter(key: String): String = null

  def getInitParameterNames(): java.util.Enumeration[String] =
    new Vector[String]().elements

  def getServletContext(): ServletContext = servletContext
}

/** A FilterChain that does nothing.
  *
  * @author
  *   Steve Jenson (stevej@pobox.com)
  */
class DoNothingFilterChain extends FilterChain with Logger {

  def doFilter(req: ServletRequest, res: ServletResponse): Unit = {
    debug("Doing nothing on filter chain")
  }

}

/** A Mock ServletInputStream. Pass in any ol InputStream like a
  * ByteArrayInputStream.
  *
  * @author
  *   Steve Jenson (stevej@pobox.com)
  */
class MockServletInputStream(is: InputStream) extends ServletInputStream {

  def read() = is.read()

  def isFinished(): Boolean = is.available() > 0

  def isReady(): Boolean = true

  def setReadListener(readListener: ReadListener): Unit = ()
}

/** A Mock ServletOutputStream. Pass in any ol' OutputStream like a
  * ByteArrayOuputStream.
  *
  * @author
  *   Steve Jenson (stevej@pobox.com)
  */
class MockServletOutputStream(os: ByteArrayOutputStream)
    extends ServletOutputStream {

  def write(b: Int): Unit = {
    os.write(b)
  }

  def isReady(): Boolean = true

  def setWriteListener(writeListener: WriteListener): Unit = ()
}

/** A Mock HttpSession implementation.
  *
  * @author
  *   Steve Jenson (stevej@pobox.com)
  */
class MockHttpSession extends HttpSession {
  @volatile protected var values: Map[String, Object] = Map()
  @volatile protected var attr: Map[String, Object] = Map()

  protected var maxii: Int = 0

  protected var creationTime: Long = System.currentTimeMillis

  def isNew = false

  def invalidate: Unit = {}

  def getValue(key: String): Object = values.get(key) match {
    case Some(v) => v
    case None    => null
  }

  def removeValue(key: String): Unit = values -= key

  def putValue(key: String, value: Object): Unit = values += (key -> value)

  def getAttribute(key: String): Object = attr.get(key) match {
    case Some(v) => v
    case None    => null
  }

  def removeAttribute(key: String): Unit = attr -= key

  def setAttribute(key: String, value: Object): Unit = attr += (key -> value)

  def getValueNames(): Array[String] = values.keys.toList.toArray

  def getAttributeNames(): java.util.Enumeration[String] =
    new java.util.Enumeration[String] {
      private val keys = attr.keys.iterator
      def hasMoreElements() = keys.hasNext
      def nextElement(): String = keys.next()
    }

  def getMaxInactiveInterval(): Int = maxii

  def setMaxInactiveInterval(i: Int): Unit = maxii = i

  def getServletContext(): ServletContext = null

  def getLastAccessedTime(): Long = 0L

  def getId(): String = null

  def getCreationTime(): Long = creationTime
}
