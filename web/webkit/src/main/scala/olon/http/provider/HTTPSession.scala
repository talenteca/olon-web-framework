package olon 
package http 
package provider 

/**
 * The representation of a HTTP session
 */
trait HTTPSession {

  /**
   * @return - the HTTP session ID
   */
  def sessionId: String

  /**
   * Links a LiftSession with this HTTP session. Hence when the HTTP session
   * terminates or times out LiftSession will be destroyed as well.
   *
   * @param liftSession - the LiftSession
   */
  def link(liftSession: LiftSession): Unit

  /**
   * The opposite of the <i>link</i>. Hence the LiftSession and the HTTP session no
   * longer needs to be related. It is called when LiftSession is explicitelly terminated.
   *
   * @param liftSession - the LiftSession
   */
  def unlink(liftSession: LiftSession): Unit

  /**
   * @return - the maximim interval in seconds between client request and the time when
   *            the session will be terminated
   *
   */
  def maxInactiveInterval: Long

  /**
   * Sets the maximim interval in seconds between client request and the time when
   * the session will be terminated
   *
   * @param interval - the value in seconds
   *
   */
  def setMaxInactiveInterval(interval: Long): Unit

  /**
   * @return - the last time server receivsd a client request for this session
   */
  def lastAccessedTime: Long

  /**
   * Sets a value associated with a name for this session
   *
   * @param name - the attribute name
   * @param value - any value
   */
  def setAttribute(name: String, value: Any): Unit

  /**
   * @param name - the attribute name
   * @return - the attribute value associated with this name
   */
  def attribute(name: String): Any

  /**
   * Removes the session attribute having this name
   *
   * @param name - the attribute name
   */
  def removeAttribute(name: String): Unit

  /**
   * Terminates this session
   */
  def terminate: Unit
}

