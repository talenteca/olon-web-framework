package olon
package mongodb
package record
package testmodels

import olon.common._
import olon.mongodb.record.field._
import olon.record.field.DoubleField

import com.mongodb._

class DoubleTest private () extends MongoRecord[DoubleTest] with ObjectIdPk[DoubleTest] {

  def meta = DoubleTest

  object doublefield extends DoubleField(this)
}

object DoubleTest extends DoubleTest with MongoMetaRecord[DoubleTest]
