package olon
package http

import util.Helpers._
import common.{Loggable, Full}


trait NamedCometActorTrait extends BaseCometActor with Loggable {

  /**
   * First thing we do is registering this comet actor
   * for the "name" key
   */
  override  def localSetup = {
    NamedCometListener.getOrAddDispatchersFor(name).foreach(
      dispatcher=> dispatcher ! registerCometActor(this, name)
    )
    super.localSetup()
  }

  /**
   * We remove the CometActor from the map of registered actors
   */
  override  def localShutdown = {
    NamedCometListener.getOrAddDispatchersFor(name).foreach(
      dispatcher=> dispatcher !  unregisterCometActor(this)
    )
    super.localShutdown()
  }

  // time out the comet actor if it hasn't been on a page for 2 minutes
  override def lifespan = Full(120.seconds)

}
