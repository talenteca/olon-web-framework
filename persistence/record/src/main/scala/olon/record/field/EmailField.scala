package olon
package record
package field

import scala.xml._
import olon.util._
import olon.common._
import olon.proto._
import olon.http.{S}
import java.util.regex._
import Helpers._
import S._

object EmailField {
  def emailPattern = ProtoRules.emailRegexPattern.vend

  def validEmailAddr_?(email: String): Boolean = emailPattern.matcher(email).matches
}

trait EmailTypedField extends TypedField[String] {
  private def validateEmail(emailValue: ValueType): List[FieldError] = {
    toBoxMyType(emailValue) match {
      case Full(email) if (optional_? && email.isEmpty) => Nil
      case Full(email) if EmailField.validEmailAddr_?(email) => Nil
      case _ => Text(S.?("invalid.email.address"))
    }
  }

  override def validations = validateEmail _ :: Nil
}

class EmailField[OwnerType <: Record[OwnerType]](@deprecatedName('rec) owner: OwnerType, maxLength: Int)
  extends StringField[OwnerType](owner, maxLength) with EmailTypedField

class OptionalEmailField[OwnerType <: Record[OwnerType]](@deprecatedName('rec) owner: OwnerType, maxLength: Int)
  extends OptionalStringField[OwnerType](owner, maxLength) with EmailTypedField

