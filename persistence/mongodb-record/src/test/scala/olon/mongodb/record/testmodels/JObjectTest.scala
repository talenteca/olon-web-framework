package olon
package mongodb
package record
package testmodels

import olon.common._
import olon.mongodb.record.field._

import com.mongodb._

class JObjectFieldTestRecord private () extends MongoRecord[JObjectFieldTestRecord]  with ObjectIdPk[JObjectFieldTestRecord] {
  def meta = JObjectFieldTestRecord

  object mandatoryJObjectField extends JObjectField(this)
}

object JObjectFieldTestRecord extends JObjectFieldTestRecord with MongoMetaRecord[JObjectFieldTestRecord] {
  override def formats = allFormats
}
