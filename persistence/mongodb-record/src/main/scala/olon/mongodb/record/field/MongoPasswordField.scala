package olon 
package mongodb 
package record 
package field 

import scala.xml.{Node, NodeSeq, Text}

import olon.common.{Box, Empty, Failure, Full}
import olon.http.S
import olon.http.js.JE._
import olon.util.{FatLazy, FieldError, Helpers, Safe}

import Helpers._

case class Password(pwd: String, salt: String) extends JsonObject[Password] {
  def meta = Password
}

object Password extends JsonObjectMeta[Password] {
  def apply(in: String): Password = Password(in, "")
}

object MongoPasswordField {
  val blankPw = "*******"

  def encrypt(s: String, salt: String) = hash("{"+s+"} salt={" + salt + "}")
}

class MongoPasswordField[OwnerType <: BsonRecord[OwnerType]](rec: OwnerType, minLen: Int) extends JsonObjectField[OwnerType, Password](rec, Password) {

  def this(rec: OwnerType) = {
    this(rec, 3)
  }

  def setPassword(in: String) = set(Password(in))

  private val salt_i = FatLazy(Safe.randomString(16))

  var validatorValue: Box[Password] = valueBox

  override def set_!(in: Box[Password]): Box[Password] = {
    validatorValue = in
    in.map(p =>
      if (p.salt.length == 0) // only encrypt the password if it hasn't already been encrypted
        Password(MongoPasswordField.encrypt(p.pwd, salt_i.get), salt_i.get)
      else
        p
    )
  }

  override def validate: List[FieldError] = runValidation(validatorValue)

  private def elem = S.fmapFunc(S.SFuncHolder(this.setPassword(_))) {
    funcName => <input type="password"
      name={funcName}
      value=""
      tabindex={tabIndex.toString}/>}

  override def toForm: Box[NodeSeq] =
    uniqueFieldId match {
      case Full(id) => Full(elem % ("id" -> id))
      case _ => Full(elem)
    }

  private def validatePassword(pwd: Password): List[FieldError] = pwd match {
    case null | Password("", _) | Password("*", _) | Password(MongoPasswordField.blankPw, _) =>
      Text(S.?("password.must.be.set"))
    case Password(pwd, _) if pwd.length < minLen =>
      Text(S.?("password.too.short"))
    case _ => Nil
  }

  override def validations = validatePassword _ :: Nil

  override def defaultValue = Password("")

  override def asJs = valueBox.map(vb => Str(vb.pwd)) openOr Str(defaultValue.pwd)

  def isMatch(toMatch: String): Boolean =
    MongoPasswordField.encrypt(toMatch, value.salt) == value.pwd
}

