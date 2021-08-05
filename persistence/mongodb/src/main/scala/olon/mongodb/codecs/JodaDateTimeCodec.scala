package olon.mongodb
package codecs

import org.joda.time.DateTime

import org.bson.codecs._
import org.bson.{BsonReader, BsonWriter}

/**
 * A Codec for joda DateTime instances.
 */
case class JodaDateTimeCodec() extends Codec[DateTime] {
  override def decode(reader: BsonReader, decoderContext: DecoderContext): DateTime = {
    new DateTime(reader.readDateTime())
  }

  override def encode(writer: BsonWriter, value: DateTime, encoderContext: EncoderContext): Unit = {
    writer.writeDateTime(value.getMillis())
  }

  override def getEncoderClass(): Class[DateTime] = classOf[DateTime]
}