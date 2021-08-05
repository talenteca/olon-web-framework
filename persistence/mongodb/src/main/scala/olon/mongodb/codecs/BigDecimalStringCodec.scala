package olon.mongodb
package codecs

import scala.math.BigDecimal

import org.bson.{BsonReader, BsonWriter}
import org.bson.codecs._
import org.bson.types.Decimal128

/**
 * A Codec for BigDecimal instances that saves the value as a String.
 */
case class BigDecimalStringCodec() extends Codec[BigDecimal] {
  override def encode(writer: BsonWriter, value: BigDecimal, encoderContext: EncoderContext): Unit = {
    writer.writeString(value.toString)
  }

  override def decode(reader: BsonReader, decoderContext: DecoderContext): BigDecimal = {
    BigDecimal(reader.readString)
  }

  override def getEncoderClass(): Class[BigDecimal] = classOf[BigDecimal]
}