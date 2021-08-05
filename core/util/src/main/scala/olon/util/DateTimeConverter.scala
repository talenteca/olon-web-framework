package olon 
package util 


import Helpers.{tryo, internetDateFormatter=>internetDateFormat, dateFormatter=>dateFormat, hourFormat, timeFormatter=>timeFormat}
import olon.common._
import java.util.Date

/**
 * Implement this trait to specify a set of rules to parse and format dates
 * @author nafg 
*/
trait DateTimeConverter {  
  /**
   * A function to format a Date as a date and time
   */
  def formatDateTime(d: Date): String
  
  /**
   * A function to format a Date as a date only
  */
  def formatDate(d: Date): String
  
  /**
   * A function to format a Date as a time.
  */
  def formatTime(d: Date): String

  /**
   * A function that parses a String representing a date and time into a Date
   */
   def parseDateTime(s: String): Box[Date]
   
  /**
   * A function that parses a String representing a date into a Date.
   */
   def parseDate(s: String): Box[Date]
  
  /**
   * A function that parses a String representing a time into a Date.
   */
   def parseTime(s: String): Box[Date]
}

/**
 * A default implementation of DateTimeConverter that uses (Time)Helpers
*/
object DefaultDateTimeConverter extends DateTimeConverter {
  def formatDateTime(d: Date) = internetDateFormat.format(d)
  def formatDate(d: Date) = dateFormat.format(d)
  /**  Uses Helpers.hourFormat which includes seconds but not time zone */
  def formatTime(d: Date) = hourFormat.format(d)
  
  def parseDateTime(s: String) = tryo { internetDateFormat.parse(s) }
  def parseDate(s: String) = tryo { dateFormat.parse(s) }
  /** Tries Helpers.hourFormat and Helpers.timeFormat */
  def parseTime(s: String) = tryo{hourFormat.parse(s)} or tryo{timeFormat.parse(s)}
}

