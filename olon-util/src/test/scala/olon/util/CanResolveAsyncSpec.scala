package olon
package util

import org.specs2.mutable.Spec

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.concurrent.Promise

import actor.LAFuture

object CanResolveAsyncSpec extends Spec {
  "CanResolveAsync" should {
    "resolve Scala Futures" in {
      val myPromise = Promise[String]()

      val resolver = implicitly[CanResolveAsync[Future[String], String]]

      val receivedResolution = new LAFuture[String]
      // SCALA3 Removing `_` for passing function as a value
      resolver.resolveAsync(myPromise.future, receivedResolution.satisfy)

      myPromise.success("All done!")

      receivedResolution.get must_== "All done!"
    }

    "resolve LAFutures" in {
      val myFuture = new LAFuture[String]

      val resolver = implicitly[CanResolveAsync[LAFuture[String], String]]

      val receivedResolution = new LAFuture[String]
      // SCALA3 Removing `_` for passing function as a value
      resolver.resolveAsync(myFuture, receivedResolution.satisfy)

      myFuture.satisfy("Got it!")

      receivedResolution.get must_== "Got it!"
    }
  }
}
