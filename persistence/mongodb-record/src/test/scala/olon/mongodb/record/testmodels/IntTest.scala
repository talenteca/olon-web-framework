package olon
package mongodb
package record
package testmodels

import olon.common._
import olon.mongodb.record.field._
import olon.record.field.IntField

import com.mongodb._

class IntTest private () extends MongoRecord[IntTest] with ObjectIdPk[IntTest] {

  def meta = IntTest

  object intfield extends IntField(this)
}

object IntTest extends IntTest with MongoMetaRecord[IntTest]
