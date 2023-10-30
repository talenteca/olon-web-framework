package olon
package http
package provider

import olon.common.Box
import olon.common.Empty
import olon.common.Full

object SameSite extends Enumeration {
  val LAX, STRICT, NONE = Value
}

/** Companion module for creating HTTPCookie objects
  */
object HTTPCookie {
  def apply(name: String, value: String) =
    new HTTPCookie(name, Full(value), Empty, Empty, Empty, Empty, Empty)
}

/** Repersents an immutable representation of an HTTP Cookie
  */
case class HTTPCookie(
    name: String,
    value: Box[String],
    domain: Box[String],
    path: Box[String],
    maxAge: Box[Int],
    version: Box[Int],
    secure_? : Box[Boolean],
    httpOnly: Box[Boolean] = Empty,
    sameSite: Box[SameSite.Value] = Empty
) extends java.lang.Cloneable {
  override def clone(): HTTPCookie = {
    super.clone()
    copy()
  }

  /** Returns a new HTTPCookie that preserve existing member values but sets the
    * httpOnly attribute
    * @param flagHttpOnly
    *   \- should the cookie be flagged as HTTP Only
    * @return
    *   HTTPCookie
    */
  def setHttpOnly(flagHttpOnly: Boolean): HTTPCookie =
    copy(httpOnly = Full(flagHttpOnly))

  /** Returns a new HTTPCookie that preserve existing member values but sets the
    * sameSite attribute
    * @param newSameSite
    *   \- the new cookie SameSite value
    * @return
    *   HTTPCookie
    */
  def setSameSite(newSameSite: SameSite.Value): HTTPCookie =
    copy(sameSite = Full(newSameSite))

  /** Returns a new HTTPCookie that preserve existing member values but sets the
    * cookie value to newValue
    * @param newValue
    *   \- the new cookie value
    * @return
    *   HTTPCookie
    */
  def setValue(newValue: String): HTTPCookie = copy(value = Box !! newValue)

  /** Returns a new HTTPCookie that preserve existing member values but sets the
    * cookie domain to newDomain
    * @param newDomain
    *   \- the new cookie domain
    * @return
    *   HTTPCookie
    */
  def setDomain(newDomain: String): HTTPCookie = copy(domain = Box !! newDomain)

  /** Returns a new HTTPCookie that preserve existing member values but sets the
    * cookie path to newPath
    * @param newPath
    *   \- the new cookie path
    * @return
    *   HTTPCookie
    */
  def setPath(newPath: String): HTTPCookie = copy(path = Box !! newPath)

  /** Returns a new HTTPCookie that preserve existing member values but sets the
    * cookie maxAge to newVMaxAge
    * @param newMaxAge
    *   \- the new cookie maxAge
    * @return
    *   HTTPCookie
    */
  def setMaxAge(newMaxAge: Int): HTTPCookie = copy(maxAge = Box !! newMaxAge)

  /** Returns a new HTTPCookie that preserve existing member values but sets the
    * cookie version to newVersion
    * @param newVersion
    *   \- the new cookie version
    * @return
    *   HTTPCookie
    */
  def setVersion(newVersion: Int): HTTPCookie =
    copy(version = Box !! newVersion)

  /** Returns a new HTTPCookie that preserve existing member values but sets the
    * cookie secure flag to newSecure
    * @param newSecure
    *   \- the new cookie secure flag
    * @return
    *   HTTPCookie
    */
  def setSecure(newSecure: Boolean): HTTPCookie =
    copy(secure_? = Box !! newSecure)

}
