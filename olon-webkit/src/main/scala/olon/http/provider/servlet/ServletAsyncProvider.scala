package olon.http.provider.servlet

import olon._

import http._
import http.provider._
import common._

/** Abstracts the management of asynchronous HTTP requests in order to allow
  * requests to be suspended and resumed later on.
  */
trait ServletAsyncProvider {

  /** @return
    *   true if the underlying JEE container supports suspend/resume
    */
  def suspendResumeSupport_? : Boolean

  /** @return
    *   the reference that was provided in the resume call
    */
  def resumeInfo: Option[(Req, LiftResponse)]

  /** Suspends this request for a given period of time
    *
    * @param timeout
    * @return
    *   a RetryState
    */
  def suspend(timeout: Long): RetryState.Value

  /** Resumes this request
    *
    * @param ref
    *   \- an object that will be associated with the resumed request
    * @return
    *   false if the resume cannot occure
    */
  def resume(ref: (Req, LiftResponse)): Boolean
}

trait AsyncProviderMeta {

  /** @return
    *   true if the underlying JEE container supports suspend/resume
    */
  def suspendResumeSupport_? : Boolean

  /** return a function that vends the ServletAsyncProvider
    */
  def providerFunction: Box[HTTPRequest => ServletAsyncProvider]
}
