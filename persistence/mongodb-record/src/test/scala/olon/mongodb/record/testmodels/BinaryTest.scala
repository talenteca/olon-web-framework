package olon
package mongodb
package record
package testmodels

import olon.common._
import olon.mongodb.record.field._
import olon.record.field.BinaryField

import com.mongodb._

class BinaryTest private () extends MongoRecord[BinaryTest] with ObjectIdPk[BinaryTest] {

  def meta = BinaryTest

  object binaryfield extends BinaryField(this) {}
}

object BinaryTest extends BinaryTest with MongoMetaRecord[BinaryTest]
