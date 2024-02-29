package olon.http

import olon.actor.LAFuture
import olon.actor.LAScheduler
import olon.common.EmptyBox
import olon.common.Failure
import olon.common.Full

object LAFutureWithSession {

  /** Creates `LAFuture` instance aware of the current request and session. Each
    * `LAFuture` returned by chained transformation method (e.g. `map`,
    * `flatMap`) will be also request/session-aware. However, it's important to
    * bear in mind that initial session or request are not propagated to chained
    * methods. It's required that current execution thread for chained method
    * has request or session available in scope if reading/writing some data to
    * it as a part of chained method execution.
    */
  def withCurrentSession[T](
      task: => T,
      scheduler: LAScheduler = LAScheduler
  ): LAFuture[T] = {
    S.session match {
      case Full(_) =>
        withSession(task, scheduler)

      case empty: EmptyBox =>
        withFailure(
          empty ?~! "LiftSession not available in this thread context",
          scheduler
        )
    }
  }

  // SCALA3 Using `private` instead of `private[this]`
  private def withSession[T](
      task: => T,
      scheduler: LAScheduler
  ): LAFuture[T] = {
    val sessionContext = new LAFuture.Context {

      def around[S](fn: () => S): () => S = {
        val session =
          S.session.openOrThrowException(
            "LiftSession not available in this thread context"
          )
        session.buildDeferredFunction(fn)
      }

      def around[A, S](fn: (A) => S): (A) => S = {
        val session =
          S.session.openOrThrowException(
            "LiftSession not available in this thread context"
          )
        session.buildDeferredFunction(fn)
      }
    }

    LAFuture.build(task, scheduler, Full(sessionContext))
  }

  // SCALA3 Using `private` instead of `private[this]`
  private def withFailure[T](
      failure: Failure,
      scheduler: LAScheduler
  ): LAFuture[T] = {
    val future = new LAFuture[T](scheduler)
    future.complete(failure)
    future
  }
}
