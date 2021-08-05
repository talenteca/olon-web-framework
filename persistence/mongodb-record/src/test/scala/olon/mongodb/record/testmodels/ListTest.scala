package olon
package mongodb
package record
package testmodels

import fixtures._

import java.util.{Date, UUID}
import java.util.regex.Pattern

import olon.common._
import olon.json._
import olon.json.ext.EnumSerializer
import olon.mongodb.codecs.{BigIntLongCodec, BsonTypeClassMap, JodaDateTimeCodec}
import olon.mongodb.record.codecs.{RecordCodec}
import olon.mongodb.record.field._
import olon.record.field.IntField

import org.bson.BsonType
import org.bson.codecs.configuration.CodecRegistries
import org.bson.types.ObjectId
import org.joda.time.DateTime

import com.mongodb._

class BasicListTestRecord private () extends MongoRecord[BasicListTestRecord] with UUIDPk[BasicListTestRecord] {
  def meta = BasicListTestRecord

  object bigIntListField extends MongoListField[BasicListTestRecord, BigInt](this)
  object binaryListField extends MongoListField[BasicListTestRecord, Array[Byte]](this)
  object booleanListField extends MongoListField[BasicListTestRecord, Boolean](this)
  object decimalListField extends MongoListField[BasicListTestRecord, BigDecimal](this)
  object doubleListField extends MongoListField[BasicListTestRecord, Double](this)
  object longListField extends MongoListField[BasicListTestRecord, Long](this)
  object stringListListField extends MongoListField[BasicListTestRecord, List[String]](this)
  object stringMapListField extends MongoListField[BasicListTestRecord, Map[String, String]](this)
}

object BasicListTestRecord extends BasicListTestRecord with MongoMetaRecord[BasicListTestRecord] {
  override def formats = allFormats

  override def codecRegistry = CodecRegistries.fromRegistries(
    CodecRegistries.fromCodecs(BigIntLongCodec()),
    RecordCodec.defaultRegistry
  )
  override def bsonTypeClassMap = RecordCodec.defaultBsonTypeClassMap
}

class ListTestRecord private () extends MongoRecord[ListTestRecord] with UUIDPk[ListTestRecord] {
  def meta = ListTestRecord

  object mandatoryStringListField extends MongoListField[ListTestRecord, String](this)
  object mandatoryMongoRefListField extends ObjectIdRefListField(this, FieldTypeTestRecord)
  object mandatoryIntListField extends MongoListField[ListTestRecord, Int](this)
  object mandatoryJsonObjectListField extends JsonObjectListField(this, TypeTestJsonObject)
  object caseClassListField extends CaseClassListField[ListTestRecord, CaseClassTestObject](this) {
    override def formats = owner.meta.formats
  }
}

object ListTestRecord extends ListTestRecord with MongoMetaRecord[ListTestRecord] {
  override def formats = allFormats + new EnumSerializer(MyTestEnum)
}


class MongoListTestRecord private () extends MongoRecord[MongoListTestRecord] with UUIDPk[MongoListTestRecord] {
  def meta = MongoListTestRecord

  object objectIdRefListField extends ObjectIdRefListField(this, FieldTypeTestRecord)

  object patternListField extends MongoListField[MongoListTestRecord, Pattern](this) {
    override def equals(other: Any): Boolean = {
      other match {
        case that: MongoListField[MongoListTestRecord, Pattern] =>
          that.value.corresponds(this.value) { (a,b) =>
            a.pattern == b.pattern && a.flags == b.flags
          }
        case _ =>
          false
      }
    }
  }

  object dateListField extends MongoListField[MongoListTestRecord, Date](this)
  object uuidListField extends MongoListField[MongoListTestRecord, UUID](this)
}

object MongoListTestRecord extends MongoListTestRecord with MongoMetaRecord[MongoListTestRecord] {
  override def formats = DefaultFormats.lossless + new ObjectIdSerializer + new PatternSerializer + new DateSerializer
}


class MongoJodaListTestRecord private () extends MongoRecord[MongoJodaListTestRecord] with UUIDPk[MongoJodaListTestRecord] {
  def meta = MongoJodaListTestRecord

  object dateTimeListField extends MongoListField[MongoJodaListTestRecord, DateTime](this)
}

object MongoJodaListTestRecord extends MongoJodaListTestRecord with MongoMetaRecord[MongoJodaListTestRecord] {
  override def formats = DefaultFormats.lossless + new DateTimeSerializer
  override def bsonTypeClassMap: BsonTypeClassMap = BsonTypeClassMap((BsonType.DATE_TIME -> classOf[DateTime]))
}
