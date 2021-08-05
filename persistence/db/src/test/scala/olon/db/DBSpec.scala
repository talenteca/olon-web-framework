package olon
package db

import org.specs2.mutable.Specification
import org.specs2.mock.Mockito

import common._
import util.DefaultConnectionIdentifier
import util.ControlHelpers._

import java.sql._

class DBSpec extends Specification with Mockito {
  sequential

  trait CommitFunc {
    def f(success: Boolean): Unit
  }

  def dBVendor(connection: Connection): ProtoDBVendor = new ProtoDBVendor {
    def createOne: Box[Connection] = {
      connection.createStatement returns mock[PreparedStatement]
      Full(connection)
    }
  }

  "eager buildLoanWrapper" should {
    "call postTransaction functions with true if transaction is committed" in {
      val m = mock[CommitFunc]
      val activeConnection = mock[Connection]
      DB.defineConnectionManager(DefaultConnectionIdentifier, dBVendor(activeConnection))

      DB.buildLoanWrapper(true) {
        DB.appendPostTransaction(DefaultConnectionIdentifier, m.f _)
        DB.currentConnection.map{c => DB.exec(c, "stuff") {dummy => }}
      }
      there was one(activeConnection).commit
      there was one(m).f(true)
    }

    "call postTransaction functions with false if transaction is rolled back" in {
      val m = mock[CommitFunc]
      val activeConnection = mock[Connection]
      DB.defineConnectionManager(DefaultConnectionIdentifier, dBVendor(activeConnection))

      val lw = DB.buildLoanWrapper(true)

      tryo(lw.apply {
        DB.appendPostTransaction(DefaultConnectionIdentifier, m.f _)
        DB.currentConnection.map{c => DB.exec(c, "stuff") {dummy => }}
        throw new RuntimeException("oh no")
        42
      })
      there was one(activeConnection).rollback
      there was one(m).f(false)
    }
  }

  "lazy buildLoanWrapper" should {
    "call postTransaction functions with true if transaction is committed" in {
      val m = mock[CommitFunc]
      val activeConnection = mock[Connection]
      DB.defineConnectionManager(DefaultConnectionIdentifier, dBVendor(activeConnection))

      DB.buildLoanWrapper(false) {
        DB.use(DefaultConnectionIdentifier) {c =>
          DB.appendPostTransaction(DefaultConnectionIdentifier, m.f _)
          DB.exec(c, "stuff") {
            dummy =>
          }
        }
        DB.use(DefaultConnectionIdentifier) {c =>
          DB.exec(c, "more stuff") { dummy => }
        }
      }
      there was one(activeConnection).commit
      there was one(m).f(true)
    }

    "call postTransaction functions with false if transaction is rolled back" in {
      val m = mock[CommitFunc]
      val activeConnection = mock[Connection]
      DB.defineConnectionManager(DefaultConnectionIdentifier, dBVendor(activeConnection))

      val lw = DB.buildLoanWrapper(false)

      tryo(lw.apply {
        DB.use(DefaultConnectionIdentifier) {c =>
          DB.exec(c, "more stuff") { dummy => }
        }
        DB.use(DefaultConnectionIdentifier) {c =>
          DB.appendPostTransaction (m.f _)
          DB.exec(c, "stuff") {dummy => throw new RuntimeException("oh no")}
        }
        42
      })
      there was one(activeConnection).rollback
      there was one(m).f(false)
    }
  }

  "DB.use" should {
    "call postTransaction functions with true if transaction is committed" in {
      val m = mock[CommitFunc]
      val activeConnection = mock[Connection]
      DB.defineConnectionManager(DefaultConnectionIdentifier, dBVendor(activeConnection))

      DB.use(DefaultConnectionIdentifier) {c =>
        DB.appendPostTransaction(DefaultConnectionIdentifier, m.f _)
        DB.exec(c, "stuff") {dummy => }
      }

      there was one(activeConnection).commit
      there was one(m).f(true)
    }

    "call postTransaction functions with false if transaction is rolled back" in {
      val m = mock[CommitFunc]
      val activeConnection = mock[Connection]
      DB.defineConnectionManager(DefaultConnectionIdentifier, dBVendor(activeConnection))

      tryo(DB.use(DefaultConnectionIdentifier) {c =>
        DB.appendPostTransaction(DefaultConnectionIdentifier, m.f _)
        DB.exec(c, "stuff") {dummy => throw new RuntimeException("Oh no")}
        42
      })

      there was one(activeConnection).rollback
      there was one(m).f(false)
      success
    }
  }

  "appendPostTransaction" should {
    "throw if called outside tx context" in {
      DB.appendPostTransaction {committed => ()}  must throwA[IllegalStateException]
    }
  }

  "DB.rollback" should {
    "call postTransaction functions with false" in {
      val m = mock[CommitFunc]
      val activeConnection = mock[Connection]
      DB.defineConnectionManager(DefaultConnectionIdentifier, dBVendor(activeConnection))

      tryo(DB.use(DefaultConnectionIdentifier) {c =>
        DB.appendPostTransaction(DefaultConnectionIdentifier, m.f _)
        DB.rollback(DefaultConnectionIdentifier)
        42
      })

      there was one(activeConnection).rollback
      there was one(m).f(false)
    }
  }
}
