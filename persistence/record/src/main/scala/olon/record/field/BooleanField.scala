package olon 
package record 
package field 

import scala.xml._
import olon.common._
import olon.http.js._
import olon.http.{S, SHtml}
import olon.json.JsonAST.{JBool, JNothing, JNull, JValue}
import olon.util._
import Helpers._
import S._
import JE._

trait BooleanTypedField extends TypedField[Boolean] {
  
  def setFromAny(in: Any): Box[Boolean] = in match{
      case b: java.lang.Boolean => setBox(Full(b.booleanValue))
      case Full(b: java.lang.Boolean) => setBox(Full(b.booleanValue))
      case Some(b: java.lang.Boolean) => setBox(Full(b.booleanValue))
      case (b: java.lang.Boolean) :: _ => setBox(Full(b.booleanValue))
      case _ => genericSetFromAny(in)
  }

  def setFromString(s: String): Box[Boolean] = 
    if(s == null || s.isEmpty) {
      if(optional_?)
    	  setBox(Empty)
       else
          setBox(Failure(notOptionalErrorMessage))
    } else {
      setBox(tryo(toBoolean(s)))
    }

  private def elem(attrs: SHtml.ElemAttr*) =
      SHtml.checkbox(valueBox openOr false, (b: Boolean) => this.setBox(Full(b)), (("tabindex" -> tabIndex.toString): SHtml.ElemAttr) :: attrs.toList: _*)

  def toForm: Box[NodeSeq] =
    // FIXME? no support for optional_?
    uniqueFieldId match {
      case Full(id) => Full(elem("id" -> id))
      case _ => Full(elem())
    }

  def asJs: JsExp = valueBox.map(boolToJsExp) openOr JsNull

  def asJValue: JValue = valueBox.map(JBool) openOr (JNothing: JValue)
  def setFromJValue(jvalue: JValue) = jvalue match {
    case JNothing|JNull if optional_? => setBox(Empty)
    case JBool(b)                     => setBox(Full(b))
    case other                        => setBox(FieldHelpers.expectedA("JBool", other))
  }
}

class BooleanField[OwnerType <: Record[OwnerType]](@deprecatedName('rec) val owner: OwnerType)
  extends Field[Boolean, OwnerType] with MandatoryTypedField[Boolean] with BooleanTypedField {

  def this(@deprecatedName('rec) owner: OwnerType, value: Boolean) = {
    this(owner)
    set(value)
  }

  def defaultValue = false
}

class OptionalBooleanField[OwnerType <: Record[OwnerType]](@deprecatedName('rec) val owner: OwnerType)
  extends Field[Boolean, OwnerType] with OptionalTypedField[Boolean] with BooleanTypedField {

  def this(@deprecatedName('rec) owner: OwnerType, value: Box[Boolean]) = {
    this(owner)
    setBox(value)
  }
}

