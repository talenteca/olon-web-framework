package olon
package json
package ext

import org.joda.time._
import org.specs2.mutable.Specification

import json.Serialization.{read, write => swrite}

/** System under specification for JodaTimeSerializer.
  */
class JodaTimeSerializerSpec extends Specification {
  "JodaTimeSerializer Specification".title

  implicit val formats: Formats =
    Serialization.formats(NoTypeHints) ++ JodaTimeSerializers.all

  "Serialize joda time types" in {
    val x = JodaTypes(
      new Duration(10 * 1000),
      new Instant(System.currentTimeMillis),
      new DateTime,
      new Interval(1000, 50000),
      new LocalDate(2011, 1, 16),
      new LocalTime(16, 52, 10),
      Period.weeks(3)
    )
    val ser = swrite(x)
    read[JodaTypes](ser) mustEqual x
  }

  "DateTime use configured date format" in {
    implicit val formats = new olon.json.DefaultFormats {
      override def dateFormatter =
        new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss'Z'")
    } ++ JodaTimeSerializers.all

    val x = Dates(new DateTime(2011, 1, 16, 10, 32, 0, 0, DateTimeZone.UTC))
    val ser = swrite(x)
    ser mustEqual """{"dt":"2011-01-16 10:32:00Z"}"""
  }

  "null is serialized as JSON null" in {
    val x = JodaTypes(null, null, null, null, null, null, null)
    val ser = swrite(x)
    read[JodaTypes](ser) mustEqual x
  }
}

case class JodaTypes(
    duration: Duration,
    instant: Instant,
    dateTime: DateTime,
    interval: Interval,
    localDate: LocalDate,
    localTime: LocalTime,
    period: Period
)

case class Dates(dt: DateTime)
