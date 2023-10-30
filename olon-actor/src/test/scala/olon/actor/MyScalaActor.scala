package olon
package actor

/** Scala implementation of LiftActor for test.
  */
class MyScalaActor extends LiftActor {
  private var value = 0

  override protected def messageHandler = {
    case Add(n) => value += n; reply(Answer(value))
    case Sub(n) => value -= n; reply(Answer(value))
    case Set(n) => value = n
    case Get()  => reply(Answer(value))
  }
}
