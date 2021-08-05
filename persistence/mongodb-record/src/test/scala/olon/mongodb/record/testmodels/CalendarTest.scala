package olon
package mongodb
package record
package testmodels

import java.util.{Date, GregorianCalendar}

import olon.common._
import olon.mongodb.codecs.{BsonTypeClassMap, CalendarCodec}
import olon.mongodb.record.field._
import olon.record.field.DateTimeField

import org.bson.BsonType
import org.bson.codecs.configuration.CodecRegistries

import com.mongodb._

class CalendarTest private () extends MongoRecord[CalendarTest] with ObjectIdPk[CalendarTest] {

  def meta = CalendarTest

  object calendarfield extends DateTimeField(this)
}

object CalendarTest extends CalendarTest with MongoMetaRecord[CalendarTest] {

  override def codecRegistry = CodecRegistries.fromRegistries(
    CodecRegistries.fromCodecs(CalendarCodec()),
    super.codecRegistry
  )

  override def bsonTypeClassMap = BsonTypeClassMap((BsonType.DATE_TIME -> classOf[GregorianCalendar]))
}
