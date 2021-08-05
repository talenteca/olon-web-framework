package olon
package record
package field

import scala.xml._
import olon.util._
import olon.common._
import olon.http.{S}
import java.util.regex._
import java.util.regex.{Pattern => RegexPattern}
import Helpers._
import S._


trait PostalCodeTypedField extends StringTypedField {

  protected val country: CountryField[_]

  override def setFilter = toUpper _ :: trim _ :: super.setFilter

  override def validations = validatePostalCode _ :: Nil

  def validatePostalCode(in: ValueType): List[FieldError] = {
    toBoxMyType(in) match {
      case Full(zip) if optional_? && zip.isEmpty => Nil
      case _ =>
        country.value match {
          case Countries.USA       => valRegex(RegexPattern.compile("[0-9]{5}(\\-[0-9]{4})?"), S.?("invalid.zip.code"))(in)
          case Countries.Sweden    => valRegex(RegexPattern.compile("[0-9]{3}[ ]?[0-9]{2}"), S.?("invalid.postal.code"))(in)
          case Countries.Australia => valRegex(RegexPattern.compile("(0?|[1-9])[0-9]{3}"), S.?("invalid.postal.code"))(in)
          case Countries.Canada    => valRegex(RegexPattern.compile("[A-Z][0-9][A-Z][ ][0-9][A-Z][0-9]"), S.?("invalid.postal.code"))(in)
          case _ => genericCheck(in)
        }
    }
  }
  private def genericCheck(zip: ValueType): List[FieldError] = {
    toBoxMyType(zip) flatMap {
      case null => Full(Text(S.?("invalid.postal.code")))
      case s if s.length < 3 => Full(Text(S.?("invalid.postal.code")))
      case _ => Empty
    }
  }
}

class PostalCodeField[OwnerType <: Record[OwnerType]](@deprecatedName('rec) owner: OwnerType, val country: CountryField[OwnerType]) extends StringField(owner, 32) with PostalCodeTypedField

class OptionalPostalCodeField[OwnerType <: Record[OwnerType]](@deprecatedName('rec) owner: OwnerType, val country: CountryField[OwnerType]) extends OptionalStringField(owner, 32) with PostalCodeTypedField

