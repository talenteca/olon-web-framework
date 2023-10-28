package olon
package http

import org.specs2.mutable.Specification

import scala.xml.NodeSeq

import actor.LAScheduler
import common._
import js.JsCmds._

class CometActorSpec extends Specification {
  private case object TestMessage

  private val testSession = new LiftSession("Test Session", "", Empty)

  private class SpecCometActor extends CometActor {
    var receivedMessages = List[Any]()

    def render = NodeSeq.Empty
    override def theSession = testSession

    override def !(msg: Any) = {
      receivedMessages ::= msg

      LAScheduler.onSameThread = true

      super.!(msg)

      LAScheduler.onSameThread = false
    }
  }

  "A CometActor" should {
    class RedirectingComet extends SpecCometActor {
      override def lowPriority = { case TestMessage =>
        S.redirectTo("place")
      }
    }

    "redirect the user when a ResponseShortcutException with redirect occurs" in {
      val comet = new RedirectingComet

      comet ! TestMessage

      comet.receivedMessages.exists {
        case PartialUpdateMsg(update) if update() == RedirectTo("place") =>
          true
        case _ =>
          false
      } must beTrue
    }

    class FunctionRedirectingComet extends SpecCometActor {
      override def lowPriority = { case TestMessage =>
        S.redirectTo("place", () => Math.random())
      }
    }

    "redirect the user with a function when a ResponseShortcutException with redirect+function occurs" in {
      val comet = new FunctionRedirectingComet

      comet ! TestMessage

      val matchingMessage =
        comet.receivedMessages.collect { case PartialUpdateMsg(update) =>
          update()
        }

      matchingMessage must beLike { case List(RedirectTo(redirectUri)) =>
        redirectUri must startWith("place")
        redirectUri must beMatching("^[^?]+\\?F[^=]+=_$".r)
      }
    }

    "be able to invoke destroySession without causing an NPE" in {
      var didRun = false
      var didThrow = false

      case object BoomSession
      val comet = new SpecCometActor {
        override def lowPriority = { case BoomSession =>
          try {
            didRun = true
            S.session.foreach(_.destroySession())
          } catch {
            case e: Exception =>
              didThrow = true
          }
        }
      }

      comet ! BoomSession

      didRun must beTrue
      didThrow must beFalse
    }
  }
}
