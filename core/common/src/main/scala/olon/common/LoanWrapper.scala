package olon 
package common 

/**
 * A component that takes action around some other functionality. It may choose
 * to execute or not execute that functionality, but should not interpret or
 * change the returned value; instead, it should perform orthogonal actions that
 * need to occur around the given functionality. A canonical example is wrapping
 * an SQL transaction around some piece of code.
 *
 * As an example, this trait defines the principal contract for function objects
 * that wrap the processing of HTTP requests in Lift.
 */
trait CommonLoanWrapper {
  /**
   * Implementations of this method may either call `f` to continue processing
   * the wrapped call as normal, or may ignore `f` to entirely replace the
   * wrapped call with a custom implementation.
   *
   * @param f the delegate which provides processing by the underlying framework
   */
  def apply[T](f: => T): T
}

object CommonLoanWrapper {
  /**
  * If you have a `List` of `LoanWrapper`s, apply them and then the
  * functions. For example:
  *
  * {{{
  * val firstWrapper = new TimerWrapper()
  * val secondWrapper = new TransactionWrapper()
  *
  * CommonLoanWrapper(firstWrapper :: secondWrapper :: Nil) {
  *   // do some things
  * })
  * }}}
  *
  * The inner code will be wrapped first in the timer and then in the
  * transaction, so that the timer will time the results of running the code
  * inside a transaction.
  */
  def apply[T, LWT <: CommonLoanWrapper](lst: List[LWT])(f: => T): T = lst match {
    case Nil => f
    case x :: xs => x.apply(this.apply(xs)(f))
  }
}

