package olon
package mongodb
package record
package testmodels

import java.math.MathContext

import olon.common._
import olon.mongodb.record.field._
import olon.mongodb.record.codecs.RecordCodec
import olon.record.field.DecimalField

import com.mongodb._

class DecimalTest private () extends MongoRecord[DecimalTest] with ObjectIdPk[DecimalTest] {

  def meta = DecimalTest

  object decimalfield extends DecimalField(this, MathContext.UNLIMITED, 2)
}

object DecimalTest extends DecimalTest with MongoMetaRecord[DecimalTest] {
  override def codecRegistry = RecordCodec.defaultRegistry
  override def bsonTypeClassMap = RecordCodec.defaultBsonTypeClassMap
}

class LegacyDecimalTest private () extends MongoRecord[LegacyDecimalTest] with ObjectIdPk[LegacyDecimalTest] {

  def meta = LegacyDecimalTest

  object decimalfield extends DecimalField(this, MathContext.UNLIMITED, 2)
}

object LegacyDecimalTest extends LegacyDecimalTest with MongoMetaRecord[LegacyDecimalTest]
