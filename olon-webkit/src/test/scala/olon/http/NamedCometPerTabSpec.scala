package olon
package http

import org.specs2.mutable.Specification

import scala.xml._

import common._
import js.JsCmds

/** System under specification for NamedComet* files.
  */
class NamedCometPerTabSpec extends Specification {
  "NamedCometPerTabSpec Specification".title

  class CometA extends NamedCometActorTrait {
    override def lowPriority = { case _ =>
      JsCmds.Noop
    }
    def render = {
      "nada" #> Text("nada")
    }
  }

  "A NamedCometDispatcher" should {
    step {
      val cometA = new CometA {
        override def name: olon.common.Full[String] = Full("1")
      }
      cometA.localSetup()

      // HACK! to ensure tests doesn't fail when trying to access actor before they've been registered
      Thread.sleep(500)
    }

    "be created for a comet" in {
      NamedCometListener
        .getDispatchersFor(Full("1"))
        .foreach(actor =>
          actor.map(_.toString must startWith("olon.http.NamedCometDispatcher"))
        )
      success
    }
    "be created even if no comet is present when calling getOrAddDispatchersFor" in {
      NamedCometListener
        .getOrAddDispatchersFor(Full("3"))
        .foreach(actor =>
          actor.toString must startWith("olon.http.NamedCometDispatcher")
        )
      success
    }
    "not be created for a non existing key" in {
      NamedCometListener
        .getDispatchersFor(Full("2"))
        .foreach(actor => actor must_== Empty)
      success
    }
  }

}
