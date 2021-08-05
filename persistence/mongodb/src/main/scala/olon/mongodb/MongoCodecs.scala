package olon.mongodb

import org.bson.codecs._

/**
  * Codec for java.lang.Long
  */
class LongPrimitiveCodec extends LongCodec {
  override def getEncoderClass() = java.lang.Long.TYPE
}

/**
  * Codec for java.lang.Integer
  */
class IntegerPrimitiveCodec extends IntegerCodec {
  override def getEncoderClass() = java.lang.Integer.TYPE
}
