package olon
package mongodb
package record

import olon.mongodb.record.codecs.RecordCodec
import olon.util.SimpleInjector

import org.bson.Transformer
import org.bson.codecs.BsonTypeClassMap
import org.bson.codecs.configuration.CodecRegistry

/**
 * MongoRecordRules holds Lift Mongo Record's configuration.
 */
object MongoRecordRules extends SimpleInjector {
  /**
   * The default CodecRegistry used.
   */
  val defaultCodecRegistry = new Inject[CodecRegistry](RecordCodec.defaultLegacyRegistry) {}

  /**
   * The default BsonTypeClassMap used.
   */
  val defaultBsonTypeClassMap = new Inject[BsonTypeClassMap](RecordCodec.defaultLegacyBsonTypeClassMap) {}

  /**
   * The default transformer used
   */
   val defaultTransformer = new Inject[Transformer](RecordCodec.defaultTransformer) {}
}
