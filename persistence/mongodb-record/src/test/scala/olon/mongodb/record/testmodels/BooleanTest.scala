package olon
package mongodb
package record
package testmodels

import olon.common._
import olon.mongodb.record.field._
import olon.record.field.BooleanField

import com.mongodb._

class BooleanTest private () extends MongoRecord[BooleanTest] with ObjectIdPk[BooleanTest] {

  def meta = BooleanTest

  object booleanfield extends BooleanField(this)
}

object BooleanTest extends BooleanTest with MongoMetaRecord[BooleanTest]
