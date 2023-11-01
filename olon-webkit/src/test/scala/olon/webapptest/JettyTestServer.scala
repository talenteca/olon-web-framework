package olon
package webapptest

import junit.framework.AssertionFailedError
import net.sourceforge.jwebunit.junit.WebTester
import org.eclipse.jetty.server.Server
import org.eclipse.jetty.webapp.WebAppContext

import java.net.URL

import common.Box

final class JettyTestServer(baseUrlBox: Box[URL]) {

  def baseUrl = baseUrlBox getOrElse new URL("http://127.0.0.1:8080")

  private val (server_, context_) = {
    val server = new Server(baseUrl.getPort)
    val context = new WebAppContext()
    context.setServer(server)
    context.setContextPath("/")
    val dir =
      System.getProperty("olon.webapptest.src.test.webapp", "src/test/webapp")
    context.setWar(dir)
    server.setHandler(context)
    server.setStopAtShutdown(true)
    (server, context)
  }

  def urlFor(path: String) = s"${baseUrl}${path}"

  def start(): Unit = {
    server_.start()
    context_.start()
  }

  def stop(): Unit = {
    context_.shutdown()
    server_.stop()
    server_.join()
  }

  def running = server_.isRunning

  def browse[A](startPath: String, f: (WebTester) => A): A = {
    val wc = new WebTester()
    try {
      wc.setScriptingEnabled(false)
      wc.beginAt(urlFor(startPath))
      f(wc)
    } catch {
      case exc: AssertionFailedError => {
        System.err.println("server response: ", wc.getServerResponse)
        throw exc
      }
    } finally {
      wc.closeBrowser()
    }
  }

}
