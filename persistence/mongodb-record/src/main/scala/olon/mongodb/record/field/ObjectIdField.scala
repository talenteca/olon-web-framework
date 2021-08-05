package olon
package mongodb
package record
package field

import java.util.Date

import olon.common.{Box, Empty, Failure, Full}
import olon.http.S
import olon.http.js.JE.{JsNull, JsRaw}
import olon.http.js.JsExp
import olon.json._
import olon.record._
import olon.util.Helpers._
import org.bson.types.ObjectId

trait ObjectIdTypedField[OwnerType <: BsonRecord[OwnerType]] extends TypedField[ObjectId] with Field[ObjectId, OwnerType] {

  def setFromAny(in: Any): Box[ObjectId] = in match {
    case oid: ObjectId => setBox(Full(oid))
    case Some(oid: ObjectId) => setBox(Full(oid))
    case Full(oid: ObjectId) => setBox(Full(oid))
    case (oid: ObjectId) :: _ => setBox(Full(oid))
    case s: String => setFromString(s)
    case Some(s: String) => setFromString(s)
    case Full(s: String) => setFromString(s)
    case null|None|Empty => setBox(defaultValueBox)
    case f: Failure => setBox(f)
    case o => setFromString(o.toString)
  }

  def setFromJValue(jvalue: JValue): Box[ObjectId] = jvalue match {
    case JNothing | JNull if optional_? => setBox(Empty)
    case JObject(JField("$oid", JString(s)) :: Nil) => setFromString(s)
    case JString(s) => setFromString(s)
    case other => setBox(FieldHelpers.expectedA("JObject", other))
  }

  def setFromString(in: String): Box[ObjectId] = {
    if (ObjectId.isValid(in)) {
      setBox (Full(new ObjectId(in)))
    } else {
      setBox(Failure(s"Invalid ObjectId string: $in"))
    }
  }

  private def elem =
    S.fmapFunc(S.SFuncHolder(this.setFromAny(_))) { funcName =>
      <input type="text"
        name={funcName}
        value={valueBox.map(s => s.toString) openOr ""}
        tabindex={tabIndex.toString}/>
    }

  def toForm = uniqueFieldId match {
    case Full(id) => Full(elem % ("id" -> id))
    case _ => Full(elem)
  }

  def asJs: JsExp = asJValue match {
    case JNothing => JsNull
    case jv => JsRaw(compactRender(jv))
  }

  def asJValue: JValue = valueBox.map(v => JsonObjectId.asJValue(v, owner.meta.formats)) openOr (JNothing: JValue)

}

class ObjectIdField[OwnerType <: BsonRecord[OwnerType]](@deprecatedName('rec) val owner: OwnerType)
  extends MandatoryTypedField[ObjectId] with ObjectIdTypedField[OwnerType] {

  def this(owner: OwnerType, value: ObjectId) = {
    this(owner)
    setBox(Full(value))
  }

  def defaultValue = new ObjectId

  def createdAt: Date = this.get.getDate

}

class OptionalObjectIdField[OwnerType <: BsonRecord[OwnerType]](val owner: OwnerType)
  extends OptionalTypedField[ObjectId] with ObjectIdTypedField[OwnerType] {

  def this(owner: OwnerType, value: Box[ObjectId]) = {
    this(owner)
    setBox(value)
  }

}
