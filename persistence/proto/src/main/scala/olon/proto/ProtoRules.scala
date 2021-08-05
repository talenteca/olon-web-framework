package olon
package proto

import olon.common._
import olon.util._
import olon.http._
import scala.reflect.Manifest

import java.util.regex.Pattern

/**
 * This singleton contains the rules for persistence
 */
object ProtoRules extends Factory with LazyLoggable {
  /**
   * The regular expression pattern for matching email addresses.
   */
  val emailRegexPattern = new FactoryMaker(Pattern.compile("^[a-z0-9._%\\-+]+@(?:[a-z0-9\\-]+\\.)+[a-z]{2,}$", Pattern.CASE_INSENSITIVE)) {}
  
}

