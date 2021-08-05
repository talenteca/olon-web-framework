package olon 
package record 
package field 

import scala.xml._
import olon.util._
import olon.common._
import olon.http.{S}
import S._
import Helpers._

trait TextareaTypedField extends StringTypedField {
  private def elem = S.fmapFunc(SFuncHolder(this.setFromAny(_))){
    funcName => <textarea name={funcName}
      rows={textareaRows.toString}
      cols={textareaCols.toString}
      tabindex={tabIndex.toString}>{valueBox openOr ""}</textarea>
  }

  override def toForm: Box[NodeSeq] =
    uniqueFieldId match {
      case Full(id) =>  Full(elem % ("id" -> id))
      case _ => Full(elem)
    }


  override def toString = valueBox match {
    case Full(s) if s.length >= 100 => s.substring(0,40) + " ... " + s.substring(s.length - 40)
    case _ => super.toString
  }

  def textareaRows  = 8

  def textareaCols = 20
}

class TextareaField[OwnerType <: Record[OwnerType]](@deprecatedName('rec) owner: OwnerType, maxLength: Int)
  extends StringField(owner, maxLength) with TextareaTypedField

class OptionalTextareaField[OwnerType <: Record[OwnerType]](@deprecatedName('rec) owner: OwnerType, maxLength: Int)
  extends OptionalStringField(owner, maxLength) with TextareaTypedField

