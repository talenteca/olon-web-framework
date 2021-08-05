package olon
package mongodb
package record
package testmodels

import olon.common._
import olon.mongodb.record.field._

import com.mongodb._

class PatternTest private () extends MongoRecord[PatternTest] with ObjectIdPk[PatternTest] {

  def meta = PatternTest

  object patternfield extends PatternField(this)
}

object PatternTest extends PatternTest with MongoMetaRecord[PatternTest]
