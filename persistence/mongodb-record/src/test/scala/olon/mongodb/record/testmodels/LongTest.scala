package olon
package mongodb
package record
package testmodels

import olon.common._
import olon.mongodb.record.field._
import olon.record.field.LongField

import com.mongodb._

class LongTest private () extends MongoRecord[LongTest] with ObjectIdPk[LongTest] {

  def meta = LongTest

  object longfield extends LongField(this)
}

object LongTest extends LongTest with MongoMetaRecord[LongTest]
