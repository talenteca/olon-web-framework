package olon.mongodb
package codecs

import scala.math.BigDecimal

import org.bson.{BsonReader, BsonWriter}
import org.bson.codecs._
import org.bson.types.Decimal128

/**
 * A Codec for BigDecimal instances.
 */
case class BigDecimalCodec() extends Codec[BigDecimal] {
  override def encode(writer: BsonWriter, value: BigDecimal, encoderContext: EncoderContext): Unit = {
    writer.writeDecimal128(new Decimal128(value.bigDecimal))
  }

  override def decode(reader: BsonReader, decoderContext: DecoderContext): BigDecimal = {
    BigDecimal(reader.readDecimal128().bigDecimalValue())
  }

  override def getEncoderClass(): Class[BigDecimal] = classOf[BigDecimal]
}