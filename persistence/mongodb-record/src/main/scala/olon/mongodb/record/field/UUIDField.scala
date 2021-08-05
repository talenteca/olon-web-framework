package olon
package mongodb
package record
package field

import java.util.UUID

import olon.common.{Box, Empty, Failure, Full}
import olon.http.S
import olon.http.js.JE.{JsNull, JsRaw}
import olon.http.js.JsExp
import olon.json._
import olon.record._
import olon.util.Helpers._

import scala.xml.NodeSeq

trait UUIDTypedField[OwnerType <: Record[OwnerType]] extends TypedField[UUID] with Field[UUID, OwnerType] {
  def setFromAny(in: Any): Box[UUID] = in match {
    case uid: UUID => setBox(Full(uid))
    case Some(uid: UUID) => setBox(Full(uid))
    case Full(uid: UUID) => setBox(Full(uid))
    case (uid: UUID) :: _ => setBox(Full(uid))
    case s: String => setFromString(s)
    case Some(s: String) => setFromString(s)
    case Full(s: String) => setFromString(s)
    case null|None|Empty => setBox(defaultValueBox)
    case f: Failure => setBox(f)
    case o => setFromString(o.toString)
  }

  def setFromJValue(jvalue: JValue): Box[UUID] = jvalue match {
    case JNothing|JNull if optional_? => setBox(Empty)
    case JObject(JField("$uuid", JString(s)) :: Nil) => setFromString(s)
    case other => setBox(FieldHelpers.expectedA("JObject", other))
  }

  def setFromString(in: String): Box[UUID] = tryo(UUID.fromString(in)) match {
    case Full(uid: UUID) => setBox(Full(uid))
    case f: Failure => setBox(f)
    case _ => setBox(Failure(s"Invalid UUID string: $in"))
  }

  private[this] def elem = S.fmapFunc(S.SFuncHolder(this.setFromAny(_))) { funcName =>
    <input type="text"
      name={funcName}
      value={valueBox.map(v => v.toString) openOr ""}
      tabindex={tabIndex.toString}/>
  }

  def toForm: Box[NodeSeq] = uniqueFieldId match {
    case Full(id) => Full(elem % ("id" -> id))
    case _ => Full(elem)
  }

  def asJs: JsExp = asJValue match {
    case JNothing => JsNull
    case jv => JsRaw(compactRender(jv))
  }

  def asJValue: JValue = valueBox.map(v => JsonUUID(v)) openOr (JNothing: JValue)
}

class UUIDField[OwnerType <: Record[OwnerType]](@deprecatedName('rec) val owner: OwnerType)
  extends UUIDTypedField[OwnerType] with MandatoryTypedField[UUID] {

  def this(owner: OwnerType, value: UUID) = {
    this(owner)
    setBox(Full(value))
  }

  def defaultValue = UUID.randomUUID

}

class OptionalUUIDField[OwnerType <: Record[OwnerType]](val owner: OwnerType)
  extends UUIDTypedField[OwnerType] with OptionalTypedField[UUID] {

  def this(owner: OwnerType, value: Box[UUID]) = {
    this(owner)
    setBox(value)
  }

}

