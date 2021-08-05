package olon
package util

import scala.concurrent.{Future, Promise}
import scala.concurrent.ExecutionContext.Implicits.global

import org.specs2.mutable.Spec

import actor.LAFuture

object CanResolveAsyncSpec extends Spec {
  "CanResolveAsync" should {
    "resolve Scala Futures" in {
      val myPromise = Promise[String]()

      val resolver = implicitly[CanResolveAsync[Future[String], String]]

      val receivedResolution = new LAFuture[String]
      resolver.resolveAsync(myPromise.future, receivedResolution.satisfy _)

      myPromise.success("All done!")

      receivedResolution.get must_== "All done!"
    }

    "resolve LAFutures" in {
      val myFuture = new LAFuture[String]

      val resolver = implicitly[CanResolveAsync[LAFuture[String], String]]

      val receivedResolution = new LAFuture[String]
      resolver.resolveAsync(myFuture, receivedResolution.satisfy _)

      myFuture.satisfy("Got it!")

      receivedResolution.get must_== "Got it!"
    }
  }
}
