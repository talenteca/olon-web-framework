package olon
package mongodb
package record
package testmodels

import scala.util.Random

import olon.common._
import olon.record._
import olon.record.field._

import org.bson._
import org.bson.codecs.{DecoderContext, EncoderContext}
import org.bson.codecs.configuration.{CodecRegistry, CodecRegistries}

import com.mongodb._
import com.mongodb.client.{MongoClients, MongoCollection}
import com.mongodb.client.model.Filters.{eq => eqs}

import olon.mongodb.codecs.{BigDecimalCodec, BsonTypeClassMap}
import olon.mongodb.record.codecs.CollectibleRecordCodec
import olon.mongodb.record.testmodels._
import olon.record._

class RecordTest private () extends Record[RecordTest] {

  def meta = RecordTest

  object id extends IntField(this) {
    override def name = "_id"
    override def defaultValue = Random.nextInt
  }

  object stringfield extends StringField(this, 100)
}

object RecordTest extends RecordTest with MetaRecord[RecordTest]

object MongoConfig {
  val defaultBsonTypeClassMap: BsonTypeClassMap =
    BsonTypeClassMap(
      (BsonType.BINARY -> classOf[Array[Byte]]),
      (BsonType.DECIMAL128 -> classOf[BigDecimal]),
      (BsonType.DOCUMENT, classOf[BsonDocument])
    )

  val defaultRegistry: CodecRegistry = CodecRegistries.fromRegistries(
    MongoClientSettings.getDefaultCodecRegistry(),
    CodecRegistries.fromCodecs(BigDecimalCodec())
  )

  private val mongoClient = MongoClients.create()

  val main = mongoClient.getDatabase("record_test_db")
}

object RecordTestStore {
  val codec = CollectibleRecordCodec(RecordTest, MongoConfig.defaultRegistry, MongoConfig.defaultBsonTypeClassMap)

  private val registry = CodecRegistries.fromRegistries(
    CodecRegistries.fromCodecs(codec),
    MongoConfig.defaultRegistry
  )

  val collection: MongoCollection[RecordTest] = MongoConfig.main.getCollection("record_test", classOf[RecordTest]).withCodecRegistry(registry)
}
