package olon
package mapper

import org.specs2.mutable.Specification

import common._
import util._
import http.{S, LiftSession}


/**
 * Systems under specification for DB.
 */
class DbSpec extends Specification  {
  "DB Specification".title

  val provider = DbProviders.H2MemoryProvider
  val logF = Schemifier.infoF _
  
  def cleanup(): Unit = {
    provider.setupDB
    Schemifier.destroyTables_!!(DefaultConnectionIdentifier, logF ,  User)
    Schemifier.schemify(true, logF, DefaultConnectionIdentifier, User)
  }
 
  "DB" should {
    "collect queries when queryCollector is added as logFunc" in {
      cleanup()
      DB.addLogFunc(DB.queryCollector)
      
      var statements: List[(String, Long)] = Nil
                           
      S.addAnalyzer((r,t,ss) => statements=ss)
      
      val session = new LiftSession("hello", "", Empty)
      val elwood = S.initIfUninitted(session) {
        val r = User.find(By(User.firstName, "Elwood"))
        S.queryLog.size must_== 1
        r
      }
      statements.size must_== 1
      elwood.map( _.firstName.get) must_== Full("Elwood")
    }
  }
}

