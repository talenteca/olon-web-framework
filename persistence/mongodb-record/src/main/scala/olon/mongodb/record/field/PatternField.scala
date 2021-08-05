package olon
package mongodb
package record
package field

import java.util.regex.Pattern

import olon.common.{Box, Empty, Failure, Full}
import olon.http.js.JE.{JsNull, Str}
import olon.json._
import olon.record.{Field, FieldHelpers, MandatoryTypedField, OptionalTypedField, Record}
import olon.util.Helpers.tryo

import scala.xml.NodeSeq

abstract class PatternTypedField[OwnerType <: Record[OwnerType]](val owner: OwnerType) extends Field[Pattern, OwnerType] {
  def setFromAny(in: Any): Box[Pattern] = in match {
    case p: Pattern => setBox(Full(p))
    case Some(p: Pattern) => setBox(Full(p))
    case Full(p: Pattern) => setBox(Full(p))
    case (p: Pattern) :: _ => setBox(Full(p))
    case s: String => setFromString(s)
    case Some(s: String) => setFromString(s)
    case Full(s: String) => setFromString(s)
    case null|None|Empty => setBox(defaultValueBox)
    case f: Failure => setBox(f)
    case o => {
      setFromString(o.toString)
    }
  }

  def setFromJValue(jvalue: JValue): Box[Pattern] = jvalue match {
    case JNothing|JNull if optional_? => setBox(Empty)
    case JObject(JField("$regex", JString(s)) :: JField("$flags", JInt(f)) :: Nil) =>
      setBox(Full(Pattern.compile(s, f.intValue)))
    case other => setBox(FieldHelpers.expectedA("JObject", other))
  }

  // parse String into a JObject
  def setFromString(in: String): Box[Pattern] = tryo(JsonParser.parse(in)) match {
    case Full(jv: JValue) => setFromJValue(jv)
    case f: Failure => setBox(f)
    case other => setBox(Failure("Error parsing String into a JValue: "+in))
  }

  def toForm: Box[NodeSeq] = Empty

  def asJs = asJValue match {
    case JNothing => JsNull
    case jv => Str(compactRender(jv))
  }

  def asJValue: JValue = valueBox.map(v => JsonRegex(v)) openOr (JNothing: JValue)

  override def equals(other: Any): Boolean = other match {
    case that: PatternTypedField[OwnerType] =>
      (that.valueBox, this.valueBox) match {
        case (Full(a), Full(b)) =>
          a.pattern == b.pattern &&
          a.flags == b.flags
        case _ =>
          that.valueBox == this.valueBox
      }
    case _ =>
      false
  }
}

class PatternField[OwnerType <: Record[OwnerType]](@deprecatedName('rec) owner: OwnerType) extends PatternTypedField[OwnerType](owner) with MandatoryTypedField[Pattern] {
  def this(owner: OwnerType, value: Pattern) = {
    this(owner)
    setBox(Full(value))
  }

  def defaultValue = Pattern.compile("")
}

class OptionalPatternField[OwnerType <: Record[OwnerType]](owner: OwnerType) extends PatternTypedField[OwnerType](owner) with OptionalTypedField[Pattern] {
  def this(owner: OwnerType, value: Box[Pattern]) = {
    this(owner)
    setBox(value)
  }
}
