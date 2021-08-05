package olon
package mongodb
package record
package testmodels

import olon.mongodb.record.field._

class DateTest private () extends MongoRecord[DateTest] with ObjectIdPk[DateTest] {

  def meta = DateTest

  object datefield extends DateField(this)
}

object DateTest extends DateTest with MongoMetaRecord[DateTest]
