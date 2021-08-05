package olon
package mongodb
package record
package testmodels

import olon.common._
import olon.mongodb.record.field._
import olon.record.field.{OptionalStringField, StringField}

import com.mongodb._

class StringTest private () extends MongoRecord[StringTest] with ObjectIdPk[StringTest] {

  def meta = StringTest

  object stringfield extends StringField(this, 100)
  object optstringfield extends OptionalStringField(this, 100)
  object stringfieldopt extends StringField(this, 100) {
    override def optional_? = true
  }
}

object StringTest extends StringTest with MongoMetaRecord[StringTest]
