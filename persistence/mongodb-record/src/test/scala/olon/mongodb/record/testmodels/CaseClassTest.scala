package olon
package mongodb
package record
package testmodels

import olon.common._
import olon.mongodb.record.field._

import com.mongodb._

case class TestCaseClass(a: String, b: Int)

class CaseClassTest private () extends MongoRecord[CaseClassTest] with ObjectIdPk[CaseClassTest] {

  def meta = CaseClassTest

  object caseclassfield extends CaseClassField[CaseClassTest, TestCaseClass](this)
}

object CaseClassTest extends CaseClassTest with MongoMetaRecord[CaseClassTest]
