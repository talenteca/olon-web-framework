package olon
package util

import common._

/** This is a decorator for a ThreadLocal variable that provides convenience
  * methods to transform the variable to a Box and execute functions in a
  * "scope" wherein the variable may hold a different value.
  */
class ThreadGlobal[T] {
  private val threadLocal = new ThreadLocal[T]

  /** Returns the current value of this variable.
    */
  def value: T = threadLocal.get

  /** Returns a Box containing the value of this ThreadGlobal in a null-safe
    * fashion.
    */
  def box: Box[T] = Box !! value

  /** Sets the value of this ThreadGlobal.
    * @param v
    *   the value to set.
    */
  def set(v: T): ThreadGlobal[T] = {
    threadLocal.set(v)
    this
  }

  /** Alias for <code>set(v: T)</code>
    * @param v
    *   the value to set.
    */
  def apply(v: T) = set(v)

  /** Sets this ThreadGlobal's contents to the specified value, executes the
    * specified function, and then restores the ThreadGlobal to its earlier
    * value. This effectively creates a scope within the execution of the
    * current thread for the execution of the specified function.
    *
    * @param x
    *   the value to temporarily set in this ThreadGlobal
    * @param f
    *   the function to execute
    */
  def doWith[R](x: T)(f: => R): R = {
    val original = value
    try {
      threadLocal.set(x)
      f
    } finally {
      threadLocal.set(original)
    }
  }
}

trait DynoVar[T] {
  private val threadLocal = new ThreadLocal[T]
  // threadLocal.set(Empty)

  def is: Box[T] = Box !! threadLocal.get

  def get = is

  def set(v: T): this.type = {
    threadLocal.set(v)
    this
  }

  def run[S](x: T)(f: => S): S = {
    val original = threadLocal.get
    try {
      threadLocal.set(x)
      f
    } finally {
      threadLocal.set(original)
    }
  }
}
