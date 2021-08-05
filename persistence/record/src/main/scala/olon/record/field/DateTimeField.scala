package olon
package record
package field

import scala.xml._
import olon.common._
import olon.http.{S}
import olon.http.js._
import olon.json._
import olon.util._
import java.util.{Calendar, Date}
import Helpers._
import S._
import JE._

trait DateTimeTypedField extends TypedField[Calendar] {
  private final def dateToCal(d: Date): Calendar = {
    val cal = Calendar.getInstance()
    cal.setTime(d)
    cal
  }

  val formats = new DefaultFormats {
    override def dateFormatter = Helpers.internetDateFormatter
  }

  def setFromAny(in : Any): Box[Calendar] = toDate(in).flatMap(d => setBox(Full(dateToCal(d)))) or genericSetFromAny(in)

  def setFromString(s: String): Box[Calendar] = s match {
    case null|"" if optional_? => setBox(Empty)
    case null|"" => setBox(Failure(notOptionalErrorMessage))
    case other => setBox(tryo(dateToCal(parseInternetDate(s))))
  }

  private def elem =
    S.fmapFunc(SFuncHolder(this.setFromAny(_))){funcName =>
      <input type={formInputType}
        name={funcName}
        value={valueBox.map(s => toInternetDate(s.getTime)) openOr ""}
        tabindex={tabIndex.toString}/>
    }

  def toForm: Box[NodeSeq] =
    uniqueFieldId match {
      case Full(id) => Full(elem % ("id" -> id))
      case _        => Full(elem)
    }

  def asJs = valueBox.map(v => Str(formats.dateFormat.format(v.getTime))) openOr JsNull

  def asJValue: JValue = asJString(v => formats.dateFormat.format(v.getTime))
  def setFromJValue(jvalue: JValue) = setFromJString(jvalue) {
    v => formats.dateFormat.parse(v).map(d => {
      val cal = Calendar.getInstance
      cal.setTime(d)
      cal
    })
  }
}

class DateTimeField[OwnerType <: Record[OwnerType]](@deprecatedName('rec) val owner: OwnerType)
  extends Field[Calendar, OwnerType] with MandatoryTypedField[Calendar] with DateTimeTypedField {

  def this(@deprecatedName('rec) owner: OwnerType, value: Calendar) = {
    this(owner)
    setBox(Full(value))
  }

  def defaultValue = Calendar.getInstance
}

class OptionalDateTimeField[OwnerType <: Record[OwnerType]](@deprecatedName('rec) val owner: OwnerType)
  extends Field[Calendar, OwnerType] with OptionalTypedField[Calendar] with DateTimeTypedField {

  def this(@deprecatedName('rec) owner: OwnerType, value: Box[Calendar]) = {
    this(owner)
    setBox(value)
  }
}

