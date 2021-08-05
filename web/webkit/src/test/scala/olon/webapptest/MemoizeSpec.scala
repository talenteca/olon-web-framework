package olon
package webapptest

import org.specs2.mutable.Specification

import common._
import util._
import http._

object SessionInfo {
  lazy val session1 = new LiftSession("/", Helpers.randomString(20), Empty)
  lazy val session2 = new LiftSession("/", Helpers.randomString(20), Empty)

  object sessionMemo extends SessionMemoize[Int, Int]
  object requestMemo extends RequestMemoize[Int, Int]
}


/**
 * System under specification for Memoize.
 */
class MemoizeSpec extends Specification  {
  "Memoize Specification".title
  sequential

  import SessionInfo._

  "Memoize" should {
    "Session memo should default to empty" >> {
      S.initIfUninitted(session1) {
        sessionMemo.get(3) must_== Empty
      }
    }

    "Session memo should be settable" >> {
      S.initIfUninitted(session1) {
        sessionMemo.get(3, 8) must_== 8

        sessionMemo.get(3) must_== Full(8)
      }
    }

    "Session memo should survive across calls" >> {
      S.initIfUninitted(session1) {
        sessionMemo.get(3) must_== Full(8)
      }
    }

    "Session memo should not float across sessions" >> {
      S.initIfUninitted(session2) {
        sessionMemo.get(3) must_== Empty
      }
    }

    "Request memo should work in the same request" >> {
      S.initIfUninitted(session1) {
        requestMemo(3) must_== Empty
        requestMemo(3, 44) must_== 44
        requestMemo(3) must_== Full(44)
      }
    }

    "Request memo should not span requests" >> {
      S.initIfUninitted(session1) {
        requestMemo(3) must_== Empty
      }
    }

  }
}

