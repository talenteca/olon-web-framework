package olon
package util

import Helpers._
import common._

/**
 * Function object that can be used in Logger.setup
 * 
 * Tries to determine which logging backend is available and configures it
 * by using either defaults or a mode-dependent configuration file.
 * 
 * To provide your own configuration, add either a log4j.props file or log4j.xml
 * file to your classpath. If using logback, name it logback.xml
 *
 * If you want to provide a configuration file for a subset of your application
 * or for a specific environment, Lift expects configuration files to be named
 * in a manner relating to the context in which they are being used. The standard
 * name format is:
 *
 * <pre>
 *   modeName.hostName.userName.filename.extension
 * </pre>
 *
 * with hostName and userName being optional, and modeName being one of
 * 'test', 'staging', 'production', 'pilot', 'profile', or 'default.
 * Thus, if you name your log4j config file 'default.log4j.xml' or
 * 'default.log4j.props' it will be picked up correctly.
 * 
 */
object LoggingAutoConfigurer {
  import ClassHelpers._
  
  private def findTheFile(files: String*): Box[(java.net.URL)] = {
    val namesToTry = Props.toTry.flatMap(f => files.toList.map(file => f()+file))
    first(namesToTry) (name => tryo(getClass.getResource(name)).filter(_ ne null))
  }

  def apply(): () => Unit = () => {
    // Try to configure log4j only if we find the SLF4J Log4j bindings
    findClass("Log4jLoggerAdapter",List("org.slf4j.impl")) map {_ =>
      findTheFile("log4j.xml", "log4j.props") match {
        case Full(url) => _root_.olon.common.Log4j.withFile(url)()
        case _ => _root_.olon.common.Log4j.withConfig(Log4j.defaultProps)()
      }
    }

    
    // Try to configure logback
    findClass("Logger", List("ch.qos.logback.classic")) map {_ =>
      findTheFile("logback.xml") map {url => Logback.withFile(url)()}
    }
    ()
  }
}
