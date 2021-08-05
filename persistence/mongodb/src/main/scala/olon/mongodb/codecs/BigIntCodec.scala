package olon.mongodb
package codecs

import scala.math.BigInt

import org.bson.{BsonReader, BsonWriter}
import org.bson.codecs._

/**
 * A Codec for BigInt instances. Values are stored as INT64.
 */
case class BigIntLongCodec() extends Codec[BigInt] {
  override def encode(writer: BsonWriter, value: BigInt, encoderContext: EncoderContext): Unit = {
    writer.writeInt64(value.longValue)
  }

  override def decode(reader: BsonReader, decoderContext: DecoderContext): BigInt = {
    BigInt(reader.readInt64())
  }

  override def getEncoderClass(): Class[BigInt] = classOf[BigInt]
}