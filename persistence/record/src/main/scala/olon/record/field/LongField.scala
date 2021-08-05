package olon 
package record 
package field 

import scala.xml._
import olon.common._
import olon.http.{S}
import json._
import olon.util._
import Helpers._
import S._

trait LongTypedField extends NumericTypedField[Long] {
  
  def setFromAny(in: Any): Box[Long] = setNumericFromAny(in, _.longValue)

  def setFromString(s: String): Box[Long] = 
    if(s == null || s.isEmpty) {
      if(optional_?)
    	  setBox(Empty)
       else
          setBox(Failure(notOptionalErrorMessage))
    } else {
      setBox(asLong(s))
    }

  def defaultValue = 0L

  def asJValue: JValue = valueBox.map(l => JInt(BigInt(l))) openOr (JNothing: JValue)
  def setFromJValue(jvalue: JValue): Box[Long] = jvalue match {
    case JNothing|JNull if optional_? => setBox(Empty)
    case JInt(i)                      => setBox(Full(i.longValue))
    case JDouble(d)                   => setBox(Full(d.toLong))
    case other                        => setBox(FieldHelpers.expectedA("JLong", other))
  }
}

class LongField[OwnerType <: Record[OwnerType]](@deprecatedName('rec) val owner: OwnerType)
  extends Field[Long, OwnerType] with MandatoryTypedField[Long] with LongTypedField {

  def this(@deprecatedName('rec) owner: OwnerType, value: Long) = {
    this(owner)
    set(value)
  }
}

class OptionalLongField[OwnerType <: Record[OwnerType]](@deprecatedName('rec) val owner: OwnerType)
  extends Field[Long, OwnerType] with OptionalTypedField[Long] with LongTypedField {

  def this(@deprecatedName('rec) owner: OwnerType, value: Box[Long]) = {
    this(owner)
    setBox(value)
  }
}

