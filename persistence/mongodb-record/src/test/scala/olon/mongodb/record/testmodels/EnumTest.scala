package olon
package mongodb
package record
package testmodels

import olon.common._
import olon.mongodb.record.field._
import olon.record.Field
import olon.record.field._

import com.mongodb._

import org.bson._
import org.bson.codecs._
import org.bson.codecs.configuration.{CodecProvider, CodecRegistries, CodecRegistry}

object TestEnum extends Enumeration {
  val One = Value("One")
  val Two = Value("Two")
  val Three = Value("Three")
}

class EnumTest private () extends MongoRecord[EnumTest] with ObjectIdPk[EnumTest] {

  def meta = EnumTest

  object enumfield extends EnumField(this, TestEnum)
  object enumnamefield extends EnumNameField(this, TestEnum)
}

object EnumTest extends EnumTest with MongoMetaRecord[EnumTest]
