package olon
package mongodb
package codecs

import java.util.{Calendar, GregorianCalendar}

import org.bson.codecs._
import org.bson.{BsonReader, BsonWriter}

/**
 * A Codec for Calendar instances.
 */
case class CalendarCodec() extends Codec[GregorianCalendar] {
  override def decode(reader: BsonReader, decoderContext: DecoderContext): GregorianCalendar = {
    val cal = new GregorianCalendar()
    cal.setTimeInMillis(reader.readDateTime())
    cal
  }

  override def encode(writer: BsonWriter, value: GregorianCalendar, encoderContext: EncoderContext): Unit = {
    writer.writeDateTime(value.getTimeInMillis())
  }

  override def getEncoderClass(): Class[GregorianCalendar] = classOf[GregorianCalendar]
}