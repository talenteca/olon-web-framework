package olon
package json
package ext

import org.joda.time._

object JodaTimeSerializers {
  def all = List(
    DurationSerializer,
    InstantSerializer,
    DateTimeSerializer,
    IntervalSerializer(),
    LocalDateSerializer(),
    LocalTimeSerializer(),
    PeriodSerializer
  )
}

case object PeriodSerializer
    extends CustomSerializer[Period](_ =>
      (
        {
          case JString(p) => new Period(p)
          case JNull      => null
        },
        { case p: Period =>
          JString(p.toString)
        }
      )
    )

case object DurationSerializer
    extends CustomSerializer[Duration](_ =>
      (
        {
          case JInt(d) => new Duration(d.longValue)
          case JNull   => null
        },
        { case d: Duration =>
          JInt(d.getMillis)
        }
      )
    )

case object InstantSerializer
    extends CustomSerializer[Instant](_ =>
      (
        {
          case JInt(i) => new Instant(i.longValue)
          case JNull   => null
        },
        { case i: Instant =>
          JInt(i.getMillis)
        }
      )
    )

object DateParser {
  def parse(s: String, format: Formats) =
    format.dateFormat
      .parse(s)
      .map(_.getTime)
      .getOrElse(throw new MappingException("Invalid date format " + s))
}

case object DateTimeSerializer
    extends CustomSerializer[DateTime](format =>
      (
        {
          case JString(s) => new DateTime(DateParser.parse(s, format))
          case JNull      => null
        },
        { case d: DateTime =>
          JString(format.dateFormat.format(d.toDate))
        }
      )
    )

private[ext] case class _Interval(start: Long, end: Long)
object IntervalSerializer {
  def apply() = new ClassSerializer(new ClassType[Interval, _Interval]() {
    def unwrap(i: _Interval)(implicit format: Formats) =
      new Interval(i.start, i.end)
    def wrap(i: Interval)(implicit format: Formats) =
      _Interval(i.getStartMillis, i.getEndMillis)
  })
}

private[ext] case class _LocalDate(year: Int, month: Int, day: Int)
object LocalDateSerializer {
  def apply() = new ClassSerializer(new ClassType[LocalDate, _LocalDate]() {
    def unwrap(d: _LocalDate)(implicit format: Formats) =
      new LocalDate(d.year, d.month, d.day)
    def wrap(d: LocalDate)(implicit format: Formats) =
      _LocalDate(d.getYear(), d.getMonthOfYear, d.getDayOfMonth)
  })
}

private[ext] case class _LocalTime(
    hour: Int,
    minute: Int,
    second: Int,
    millis: Int
)
object LocalTimeSerializer {
  def apply() = new ClassSerializer(new ClassType[LocalTime, _LocalTime]() {
    def unwrap(t: _LocalTime)(implicit format: Formats) =
      new LocalTime(t.hour, t.minute, t.second, t.millis)
    def wrap(t: LocalTime)(implicit format: Formats) =
      _LocalTime(
        t.getHourOfDay,
        t.getMinuteOfHour,
        t.getSecondOfMinute,
        t.getMillisOfSecond
      )
  })
}

private[ext] trait ClassType[A, B] {
  def unwrap(b: B)(implicit format: Formats): A
  def wrap(a: A)(implicit format: Formats): B
}

case class ClassSerializer[A: Manifest, B: Manifest](t: ClassType[A, B])
    extends Serializer[A] {
  private val Class = implicitly[Manifest[A]].runtimeClass

  def deserialize(implicit
      format: Formats
  ): PartialFunction[(TypeInfo, JValue), A] = {
    case (TypeInfo(Class, _), json) =>
      json match {
        case JNull => null.asInstanceOf[A]
        case xs: JObject if (xs.extractOpt[B].isDefined) =>
          t.unwrap(xs.extract[B])
        case value =>
          throw new MappingException("Can't convert " + value + " to " + Class)
      }
  }

  def serialize(implicit format: Formats): PartialFunction[Any, JValue] = {
    case a: A if a.asInstanceOf[AnyRef].getClass == Class =>
      Extraction.decompose(t.wrap(a))
  }
}
