package olon
package record

import olon.http.S
import olon.http.S._
import olon.http.js._
import JsCmds._
import scala.xml.{NodeSeq, Node, Text, Elem}
import scala.xml.transform._
import olon.sitemap._
import olon.sitemap.Loc._
import olon.util.Helpers._
import olon.util._
import olon.common._
import olon.util.Mailer._
import olon.record.field._
import olon.proto.{ProtoUser => GenProtoUser}

/**
 * ProtoUser provides a "User" with a first name, last name, email, etc.
 */
trait ProtoUser[T <: ProtoUser[T]] extends Record[T] {
  self: T =>

  /**
   * The primary key field for the User.  You can override the behavior
   * of this field:
   * <pre name="code" class="scala">
   * override lazy val id = new MyMappedLongClass(this) {
   *   println("I am doing something different")
   * }
   * </pre>
   */
  lazy val id: LongField[T] = new MyMappedLongClass(this)

  protected class MyMappedLongClass(obj: T) extends LongField(obj)

  /**
   * Convert the id to a String
   */
  def userIdAsString: String = id.get.toString
  
  /**
   * The first name field for the User.  You can override the behavior
   * of this field:
   * <pre name="code" class="scala">
   * override lazy val firstName = new MyFirstName(this, 32) {
   *   println("I am doing something different")
   * }
   * </pre>
   */

  lazy val firstName: StringField[T] = new MyFirstName(this, 32)

  protected class MyFirstName(obj: T, size: Int) extends StringField(obj, size) {
    override def displayName = owner.firstNameDisplayName
    override val fieldId = Some(Text("txtFirstName"))
  }

  /**
   * The string name for the first name field
   */
  def firstNameDisplayName = S.?("first.name")

  /**
   * The last field for the User.  You can override the behavior
   * of this field:
   * <pre name="code" class="scala">
   * override lazy val lastName = new MyLastName(this, 32) {
   *   println("I am doing something different")
   * }
   * </pre>
   */
  lazy val lastName: StringField[T] = new MyLastName(this, 32)

  protected class MyLastName(obj: T, size: Int) extends StringField(obj, size) {
    override def displayName = owner.lastNameDisplayName
    override val fieldId = Some(Text("txtLastName"))
  }

  /**
   * The last name string
   */
  def lastNameDisplayName = S.?("last.name")

  /**
   * The email field for the User.  You can override the behavior
   * of this field:
   * <pre name="code" class="scala">
   * override lazy val email = new MyEmail(this, 48) {
   *   println("I am doing something different")
   * }
   * </pre>
   */
  lazy val email: EmailField[T] = new MyEmail(this, 48)

  protected class MyEmail(obj: T, size: Int) extends EmailField(obj, size) {
    override def validations = valUnique(S.?("unique.email.address")) _ :: super.validations
    override def displayName = owner.emailDisplayName
    override val fieldId = Some(Text("txtEmail"))
  }

  protected def valUnique(errorMsg: => String)(email: String): List[FieldError]

  /**
   * The email first name
   */
  def emailDisplayName = S.?("email.address")

  /**
   * The password field for the User.  You can override the behavior
   * of this field:
   * <pre name="code" class="scala">
   * override lazy val password = new MyPassword(this) {
   *   println("I am doing something different")
   * }
   * </pre>
   */
  lazy val password: PasswordField[T] = new MyPassword(this)

  protected class MyPassword(obj: T) extends PasswordField(obj) {
    override def displayName = owner.passwordDisplayName
  }

  /**
   * The display name for the password field
   */
  def passwordDisplayName = S.?("password")

  /**
   * The superuser field for the User.  You can override the behavior
   * of this field:
   * <pre name="code" class="scala">
   * override lazy val superUser = new MySuperUser(this) {
   *   println("I am doing something different")
   * }
   * </pre>
   */  
  lazy val superUser: BooleanField[T] = new MySuperUser(this)

  protected class MySuperUser(obj: T) extends BooleanField(obj) {
    override def defaultValue = false
  }

  def niceName: String = (firstName.get, lastName.get, email.get) match {
    case (f, l, e) if f.length > 1 && l.length > 1 => f+" "+l+" ("+e+")"
    case (f, _, e) if f.length > 1 => f+" ("+e+")"
    case (_, l, e) if l.length > 1 => l+" ("+e+")"
    case (_, _, e) => e
  }

  def shortName: String = (firstName.get, lastName.get) match {
    case (f, l) if f.length > 1 && l.length > 1 => f+" "+l
    case (f, _) if f.length > 1 => f
    case (_, l) if l.length > 1 => l
    case _ => email.get
  }

  def niceNameWEmailLink = <a href={"mailto:"+email.get}>{niceName}</a>
}

/**
 * Mix this trait into the Mapper singleton for User and you
 * get a bunch of user functionality including password reset, etc.
 */
trait MetaMegaProtoUser[ModelType <: MegaProtoUser[ModelType]] extends MetaRecord[ModelType] with GenProtoUser {
  self: ModelType =>

  type TheUserType = ModelType

  /**
   * What's a field pointer for the underlying CRUDify
   */
  type FieldPointerType = Field[_, TheUserType]

  /**
   * Based on a FieldPointer, build a FieldPointerBridge
   */
  protected implicit def buildFieldBridge(from: FieldPointerType): FieldPointerBridge = new MyPointer(from)


  protected class MyPointer(from: FieldPointerType) extends FieldPointerBridge {
    /**
     * What is the display name of this field?
     */
    def displayHtml: NodeSeq = from.displayHtml

    /**
     * Does this represent a pointer to a Password field?
     */
    def isPasswordField_? : Boolean = from match {
      case a: PasswordField[_] => true
      case _ => false
    }
  }

  /**
   * Convert an instance of TheUserType to the Bridge trait
   */
  protected implicit def typeToBridge(in: TheUserType): UserBridge = 
    new MyUserBridge(in)

  /**
   * Bridges from TheUserType to methods used in this class
   */
  protected class MyUserBridge(in: TheUserType) extends UserBridge {
    /**
     * Convert the user's primary key to a String
     */
    def userIdAsString: String = in.id.toString

    /**
     * Return the user's first name
     */
    def getFirstName: String = in.firstName.get

    /**
     * Return the user's last name
     */
    def getLastName: String = in.lastName.get

    /**
     * Get the user's email
     */
    def getEmail: String = in.email.get

    /**
     * Is the user a superuser
     */
    def superUser_? : Boolean = in.superUser.get

    /**
     * Has the user been validated?
     */
    def validated_? : Boolean = in.validated.get

    /**
     * Does the supplied password match the actual password?
     */
    def testPassword(toTest: Box[String]): Boolean = 
      toTest.map(in.password.match_?) openOr false

    /**
     * Set the validation flag on the user and return the user
     */
    def setValidated(validation: Boolean): TheUserType =
      in.validated(validation)

    /**
     * Set the unique ID for this user to a new value
     */
    def resetUniqueId(): TheUserType = {
      in.uniqueId.reset()
    }

    /**
     * Return the unique ID for the user
     */
    def getUniqueId(): String = in.uniqueId.get

    /**
     * Validate the user
     */
    def validate: List[FieldError] = in.validate

    /**
     * Given a list of string, set the password
     */
    def setPasswordFromListString(pwd: List[String]): TheUserType = {
      in.password.setFromAny(pwd)
      in
    }

    /**
     * Save the user to backing store
     */
    def save(): Boolean = in.saveTheRecord().isDefined
  }

  /**
   * Given a field pointer and an instance, get the field on that instance
   */
  protected def computeFieldFromPointer(instance: TheUserType, pointer: FieldPointerType): Box[BaseField] = fieldByName(pointer.name, instance)


  /**
   * Given an username (probably email address), find the user
   */
  protected def findUserByUserName(email: String): Box[TheUserType]

  /**
   * Given a unique id, find the user
   */
  protected def findUserByUniqueId(id: String): Box[TheUserType]

  /**
   * Create a new instance of the User
   */
  protected def createNewUserInstance(): TheUserType = self.createRecord

  /**
   * Given a String representing the User ID, find the user
   */
  protected def userFromStringId(id: String): Box[TheUserType]

  /**
   * The list of fields presented to the user at sign-up
   */
  def signupFields: List[FieldPointerType] = List(firstName, 
                                                  lastName, 
                                                  email, 
                                                  locale, 
                                                  timezone,
                                                  password)

  /**
   * The list of fields presented to the user for editing
   */
  def editFields: List[FieldPointerType] = List(firstName, 
                                                lastName, 
                                                email, 
                                                locale, 
                                                timezone)

}

/**
 * ProtoUser is bare bones.  MetaProtoUser contains a bunch
 * more fields including a validated flag, locale, timezone, etc.
 */
trait MegaProtoUser[T <: MegaProtoUser[T]] extends ProtoUser[T] {
  self: T =>

  /**
   * The unique id field for the User. This field
   * is used for validation, lost passwords, etc.
   * You can override the behavior
   * of this field:
   * <pre name="code" class="scala">
   * override lazy val uniqueId = new MyUniqueId(this, 32) {
   *   println("I am doing something different")
   * }
   * </pre>
   */
  lazy val uniqueId: UniqueIdField[T] = new MyUniqueId(this, 32)

  protected class MyUniqueId(obj: T, size: Int) extends UniqueIdField(obj, size) {

  }

  /**
   * Whether the user has been validated.
   * You can override the behavior
   * of this field:
   * <pre name="code" class="scala">
   * override lazy val validated = new MyValidated(this, 32) {
   *   println("I am doing something different")
   * }
   * </pre>
   */
  lazy val validated: BooleanField[T] = new MyValidated(this)

  protected class MyValidated(obj: T) extends BooleanField[T](obj) {
    override def defaultValue = false
    override val fieldId = Some(Text("txtValidated"))
  }

  /**
   * The locale field for the User.
   * You can override the behavior
   * of this field:
   * <pre name="code" class="scala">
   * override lazy val locale = new MyLocale(this, 32) {
   *   println("I am doing something different")
   * }
   * </pre>
   */
  lazy val locale: LocaleField[T] = new MyLocale(this)

  protected class MyLocale(obj: T) extends LocaleField[T](obj) {
    override def displayName = owner.localeDisplayName
    override val fieldId = Some(Text("txtLocale"))
  }

  /**
   * The time zone field for the User.
   * You can override the behavior
   * of this field:
   * <pre name="code" class="scala">
   * override lazy val timezone = new MyTimeZone(this, 32) {
   *   println("I am doing something different")
   * }
   * </pre>
   */
  lazy val timezone: TimeZoneField[T] = new MyTimeZone(this)

  protected class MyTimeZone(obj: T) extends TimeZoneField[T](obj) {
    override def displayName = owner.timezoneDisplayName
    override val fieldId = Some(Text("txtTimeZone"))
  }

  /**
   * The string for the timezone field
   */
  def timezoneDisplayName = S.?("time.zone")

  /**
   * The string for the locale field
   */
  def localeDisplayName = S.?("locale")

}
