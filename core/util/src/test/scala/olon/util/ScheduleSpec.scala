package olon
package util

import org.specs2.mutable.Specification
import org.specs2.specification.BeforeEach
import org.specs2.execute.PendingUntilFixed

import actor._
import Helpers._


/**
 * Systems under specification for Lift Schedule.
 */
class ScheduleSpec extends Specification with PendingUntilFixed with PingedService with BeforeEach {
  "Schedule Specification".title

  def before = Schedule.restart

  "The Schedule object" should {
    "provide a schedule method to ping an actor regularly" in {
      Schedule.schedule(service, Alive, TimeSpan(10))
      service.pinged must eventually(beTrue)
    }
    "honor multiple restarts" in {
      Schedule.restart
      Schedule.restart
      Schedule.restart
      Schedule.schedule(service, Alive, TimeSpan(10))
      service.pinged must eventually(beTrue)
    }
    "honor shutdown followed by restart" in {
      Schedule.shutdown
      Schedule.restart
      Schedule.schedule(service, Alive, TimeSpan(10))
      service.pinged must eventually(beTrue)
    }
    "not honor multiple shutdowns" in {
      Schedule.shutdown
      Schedule.shutdown
//      service.pinged must eventually(beFalse)
      service.pinged must throwA[ActorPingException]
    }.pendingUntilFixed
  }

}


trait PingedService {
  case object Alive
  val service = new Service

  class Service extends LiftActor {
    @volatile var pinged = false
    /*
    def act() {
      while (true) {
        receive {
          case Alive => {pinged = true; exit()}
        }
      }
    }
    */
    protected def messageHandler = {
          case Alive => {pinged = true /*; exit() */}
    }
  }
}
