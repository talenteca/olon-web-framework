package olon 
package record 
package field 

import olon.http.{S}
import olon.http.js._
import olon.util._
import olon.common._
import scala.reflect.Manifest
import scala.xml._
import S._
import Helpers._
import JE._

trait NumericTypedField[MyType] extends TypedField[MyType] {

  /** Augments genericSetFromAny with support for values of type Number (optionally wrapped in any of the usual suspects) */
  protected final def setNumericFromAny(in: Any, f: Number => MyType)(implicit m: Manifest[MyType]): Box[MyType] =
    in match {
      case     (n: Number) => setBox(Full(f(n)))
      case Some(n: Number) => setBox(Full(f(n)))
      case Full(n: Number) => setBox(Full(f(n)))
      case (n: Number)::_  => setBox(Full(f(n)))
      case _ => genericSetFromAny(in)
    }

  private def elem = S.fmapFunc((s: List[String]) => setFromAny(s)) {
    funcName => <input type={formInputType} name={funcName} value={valueBox.map(_.toString) openOr ""} tabindex={tabIndex.toString}/>
  }

  /**
   * Returns form input of this field
   */
  def toForm: Box[NodeSeq] =
    uniqueFieldId match {
      case Full(id) => Full(elem % ("id" -> id))
      case _ => Full(elem)
    }

  override def noValueErrorMessage = S.?("number.required")

  def asJs = valueBox.map(v => JsRaw(String.valueOf(v))) openOr JsNull

}

