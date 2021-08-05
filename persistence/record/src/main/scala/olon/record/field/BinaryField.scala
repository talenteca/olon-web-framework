package olon
package record
package field

import scala.xml._
import olon.common._
import olon.http.{S}
import olon.http.js._
import olon.json.JsonAST.JValue
import olon.util._
import Helpers._
import S._
import JE._


trait BinaryTypedField extends TypedField[Array[Byte]] {

  def setFromAny(in: Any): Box[Array[Byte]] = genericSetFromAny(in)

  def setFromString(s: String): Box[Array[Byte]] = s match {
    case null|"" if optional_? => setBox(Empty)
    case null|"" => setBox(Failure(notOptionalErrorMessage))
    case _ => setBox(tryo(s.getBytes("UTF-8")))
  }

  def toForm: Box[NodeSeq] = Empty

  def asJs = valueBox.map(v => Str(hexEncode(v))) openOr JsNull

  def asJValue: JValue = asJString(base64Encode _)
  def setFromJValue(jvalue: JValue) = setFromJString(jvalue)(s => tryo(base64Decode(s)))
}

class BinaryField[OwnerType <: Record[OwnerType]](@deprecatedName('rec) val owner: OwnerType)
  extends Field[Array[Byte], OwnerType] with MandatoryTypedField[Array[Byte]] with BinaryTypedField {

  def this(@deprecatedName('rec) owner: OwnerType, value: Array[Byte]) = {
    this(owner)
    set(value)
  }

  def defaultValue = Array(0)
}

class OptionalBinaryField[OwnerType <: Record[OwnerType]](@deprecatedName('rec) val owner: OwnerType)
  extends Field[Array[Byte], OwnerType] with OptionalTypedField[Array[Byte]] with BinaryTypedField {

  def this(@deprecatedName('rec) owner: OwnerType, value: Box[Array[Byte]]) = {
    this(owner)
    setBox(value)
  }
}

