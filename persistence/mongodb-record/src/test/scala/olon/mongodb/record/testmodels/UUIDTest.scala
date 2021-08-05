package olon
package mongodb
package record
package testmodels

import olon.common._
import olon.mongodb.record.field._

import com.mongodb._

class UUIDTest private () extends MongoRecord[UUIDTest] with ObjectIdPk[UUIDTest] {

  def meta = UUIDTest

  object uuidfield extends UUIDField(this)
}

object UUIDTest extends UUIDTest with MongoMetaRecord[UUIDTest]
