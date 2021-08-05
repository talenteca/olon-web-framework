package olon
package mongodb
package record
package testmodels

case class CaseClassTestObject(intField: Int, stringField: String, enum: MyTestEnum.Value)

object MyTestEnum extends Enumeration {
  val ONE = Value("ONE")
  val TWO = Value("TWO")
  val THREE = Value("THREE")
}
