package olon
package util

import scala.concurrent.ExecutionContext
import scala.concurrent.Future

import actor.LAFuture

/** Represents a unifying class that can resolve an asynchronous container. For
  * example, a `Future[String]` can be resolved by a
  * `CanResolveAsync[Future[String], String]`.
  *
  * Provides one method, `[[resolveAsync]]`, that takes the async container and
  * a function to run when the container resolves.
  */
trait CanResolveAsync[ResolvableType, ResolvedType] {

  /** Should return a function that, when given the resolvable and a function
    * that takes the resolved value, attaches the function to the resolvable so
    * that it will asynchronously execute it when its value is resolved.
    *
    * See `CanResolveFuture` and `CanResolveLAFuture` in `lift-webkit` for
    * example usages.
    */
  def resolveAsync(
      resolvable: ResolvableType,
      onResolved: (ResolvedType) => Unit
  ): Unit
}

trait LowPriorityCanResolveAsyncImplicits {
  self: CanResolveAsync.type =>

  // Low priority implicit for resolving Scala Futures.
  implicit def resolveFuture[T](implicit executionContext: ExecutionContext) = {
    new CanResolveAsync[Future[T], T] {
      def resolveAsync(future: Future[T], onResolved: (T) => Unit) = {
        future.foreach(onResolved)
      }
    }
  }

  // Low priority implicit for resolving Lift LAFutures.
  implicit def resolveLaFuture[T] = {
    new CanResolveAsync[LAFuture[T], T] {
      def resolveAsync(future: LAFuture[T], onResolved: (T) => Unit) = {
        future.onSuccess(onResolved)
      }
    }
  }
}
object CanResolveAsync extends LowPriorityCanResolveAsyncImplicits
