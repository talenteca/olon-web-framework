package olon.mongodb
package record
package codecs

import scala.collection.mutable
import java.util.{Calendar, ArrayList, List => JavaList, Map => JavaMap, UUID}
import java.util.regex.Pattern
import java.util.Arrays.asList

import olon.common._
import olon.mongodb.codecs._
import olon.mongodb.record.field._
import olon.record.{Field, MandatoryTypedField, MetaRecord, Record}
import olon.util.Helpers.tryo

import org.bson._
import org.bson.codecs._
import org.bson.codecs.configuration.{CodecRegistry, CodecRegistries}
import com.mongodb._
import com.mongodb.client.gridfs.codecs.GridFSFileCodecProvider

/**
 * A Collectible (requires an _id field) codec for Record instances.
 */
object CollectibleRecordCodec {
  private val idFieldName: String = "_id"
}

case class CollectibleRecordCodec[T <: Record[T]](
  metaRecord: MetaRecord[T],
  codecRegistry: CodecRegistry = RecordCodec.defaultLegacyRegistry,
  bsonTypeClassMap: BsonTypeClassMap = RecordCodec.defaultLegacyBsonTypeClassMap,
  valueTransformer: Transformer = RecordCodec.defaultTransformer
)
  extends RecordTypedCodec[T]
  with CollectibleCodec[T]
{
  override def getEncoderClass(): Class[T] = metaRecord.createRecord.getClass.asInstanceOf[Class[T]]

  /**
   * Fields must be predefined on Records so there's no way to add one if missing.
   */
  def generateIdIfAbsentFromDocument(rec: T): T = {
    rec
  }

  private def findIdField(rec: T): Option[Field[_, T]] = {
    rec.fieldByName(CollectibleRecordCodec.idFieldName).toOption
  }

  def documentHasId(rec: T): Boolean = {
    findIdField(rec).nonEmpty
  }

  def getDocumentId(rec: T): BsonValue = {
    if (!documentHasId(rec)) {
      throw new IllegalStateException("The rec does not contain an _id")
    }

    findIdField(rec) match {
      case Some(field: Field[_, T] with MandatoryTypedField[_]) =>
        val idHoldingDocument = new BsonDocument()
        val writer = new BsonDocumentWriter(idHoldingDocument)
        writer.writeStartDocument()
        writer.writeName(CollectibleRecordCodec.idFieldName)

        writeValue(writer, EncoderContext.builder().build(), field.value.asInstanceOf[Object])

        writer.writeEndDocument()
        idHoldingDocument.get(CollectibleRecordCodec.idFieldName)
      case _ =>
        throw new IllegalStateException("The _id field could not be found")
    }
  }
}
