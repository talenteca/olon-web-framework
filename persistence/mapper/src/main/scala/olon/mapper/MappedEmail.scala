package olon
package mapper

import http.S
import util.FieldError
import proto._

import scala.xml.Text

object MappedEmail {
  def emailPattern = ProtoRules.emailRegexPattern.vend

  def validEmailAddr_?(email: String): Boolean = emailPattern.matcher(email).matches
}

abstract class MappedEmail[T<:Mapper[T]](owner: T, maxLen: Int) extends MappedString[T](owner, maxLen) {

  override def setFilter = notNull _ :: toLower _ :: trim _ :: super.setFilter

  override def validate =
    (if (MappedEmail.emailPattern.matcher(i_is_!).matches) Nil else List(FieldError(this, Text(S.?("invalid.email.address"))))) :::
    super.validate

}

