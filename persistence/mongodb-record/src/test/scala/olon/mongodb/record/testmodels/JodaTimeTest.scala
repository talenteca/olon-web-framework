package olon
package mongodb
package record
package testmodels

import olon.common._
import olon.mongodb.codecs.{BsonTypeClassMap, JodaDateTimeCodec}
import olon.mongodb.record.field._
import olon.record.field.joda.JodaTimeField

import com.mongodb._

import org.bson.BsonType
import org.bson.codecs.configuration.CodecRegistries

import org.joda.time.DateTime

class JodaTimeTest private () extends MongoRecord[JodaTimeTest] with ObjectIdPk[JodaTimeTest] {

  def meta = JodaTimeTest

  object jodatimefield extends JodaTimeField(this)
}

object JodaTimeTest extends JodaTimeTest with MongoMetaRecord[JodaTimeTest] {}
