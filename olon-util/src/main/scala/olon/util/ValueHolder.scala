package olon
package util

trait ValueHolder {
  type ValueType

  /** get the value
    */
  def get: ValueType
}

/** A value that can be set
  */
trait Settable extends ValueHolder {
  def set(in: ValueType): ValueType

  /** Perform an atomic update of this Settable. The current value is passed to
    * the function and the ValueHolder is set to the result of the function.
    * This is enclosed in the performAtomicOperation method which will, by
    * default, synchronize this instance
    */
  def atomicUpdate(f: ValueType => ValueType): ValueType =
    performAtomicOperation(set(f(get)))

  /** Perform an atomic operation on the Settable. By default synchronizes the
    * instance, but it could use other mechanisms
    */
  def performAtomicOperation[T](f: => T): T = synchronized {
    f
  }
}

trait SettableValueHolder extends Settable

trait PValueHolder[T] extends ValueHolder {
  type ValueType = T
}

object PValueHolder {
  implicit def tToVHT[T](in: T): PValueHolder[T] = new PValueHolder[T] {
    def get = in
  }
  def apply[T](in: T) = tToVHT(in)
}

object ValueHolder {
  implicit def tToVHT[T](in: T): ValueHolder = new PValueHolder[T] {
    def get = in
  }
  def apply[T](in: T) = tToVHT(in)
}

trait PSettableValueHolder[T] extends PValueHolder[T] with SettableValueHolder

/** Kinda like a JavaBean property. It's something that can be set and retrieved
  */
trait LiftValue[T] extends PSettableValueHolder[T] {
  def is: T = get
}
