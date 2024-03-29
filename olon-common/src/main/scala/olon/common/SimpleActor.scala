package olon
package common

/** The simple definition of an actor. Something that can be sent a message of
  * type `T`.
  */
trait SimpleActor[-T] {

  /** Send a message to the Actor
    *
    * @param param
    *   the message to send
    */
  def !(param: T): Unit
}

/** An Actor that can receive a message of any type.
  */
trait SimplestActor extends SimpleActor[Any]

/** An Actor that can receive messsages of type `T` and return responses of type
  * `R`.
  */
trait TypedActor[-T, +R] extends SimpleActor[T] {

  /** Compatible with Scala actors' `!?` method, sends the given `message` to
    * this actor and waits infinitely for a reply.
    */
  def !?(message: T): R

  /** Compatible with Scala actors' `!?` method, sends the given `message` to
    * this actor and waits up to `timeout` for a reply, returning `[[Empty]]` or
    * `[[Failure]]` if no reply is received by then.
    */
  def !?(timeout: Long, message: Any): Box[R]

  /** Asynchronous message send. Send-and-receive eventually. Waits on a Future
    * for the reply message. If recevied within the Actor default timeout
    * interval then it returns `Full(result)` and if a timeout has occured
    * `[[Empty]]` or `[[Failure]]`.
    */
  def !!(message: T): Box[R]

  /** Asynchronous message send. Send-and-receive eventually. Waits on a Future
    * for the reply message. If recevied within timout interval that is
    * specified then it returns `Full(result)` and if a timeout has occured
    * `[[Empty]]` or `[[Failure]]`.
    */
  def !!(message: T, timeout: Long): Box[R]
}

/** Generic Actor interface. Can receive any type of message. Can return (via
  * `!!` and `!?`) messages of type `R`.
  */
trait GenericActor[+R] extends TypedActor[Any, R]

/** Generic Actor interface. Can receive any type of message. Can return (via
  * `!!` and `!?`) messages of any type.
  */
trait SimplestGenericActor extends GenericActor[Any]

/** Interface for an actor that can internally forward received messages to
  * other actors.
  */
trait ForwardableActor[From, To] {
  self: TypedActor[From, To] =>

  protected def forwardMessageTo(
      msg: From,
      forwardTo: TypedActor[From, To]
  ): Unit

  protected def reply(msg: To): Unit
}
