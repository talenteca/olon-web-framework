package olon
package http

import scala.collection.parallel.CollectionConverters._

import common.{Box, Full, Loggable}
import actor.LiftActor

/** This class keeps a list of comet actors that need to update the UI
  */
class NamedCometDispatcher(name: Box[String]) extends LiftActor with Loggable {

  logger.debug("DispatcherActor got name: %s".format(name))

  private var cometActorsToUpdate: Vector[BaseCometActor] = Vector()

  override def messageHandler = {
    /** if we do not have this actor in the list, add it (register it)
      */
    case registerCometActor(actor, Full(_)) => {
      if (cometActorsToUpdate.contains(actor) == false) {
        logger.debug("We are adding actor: %s to the list".format(actor))
        cometActorsToUpdate = cometActorsToUpdate :+ actor
      } else {
        logger.debug("The list so far is %s".format(cometActorsToUpdate))
      }
    }
    case unregisterCometActor(actor) => {
      logger.debug("before %s".format(cometActorsToUpdate))
      cometActorsToUpdate = cometActorsToUpdate.filterNot(_ == actor)
      logger.debug("after %s".format(cometActorsToUpdate))
    }

    // Catch the dummy message we send on comet creation
    case CometName(_) =>

    /** Go through the list of actors and send them a message
      */
    case msg => {
      cometActorsToUpdate.par.foreach { x =>
        {
          x ! msg
          logger.debug(
            "We will update this comet actor: %s showing name: %s".format(
              x,
              name
            )
          )
        }
      }
    }
  }
}

/** These are the message we pass around to register each named comet actor with
  * a dispatcher that only updates the specific version it monitors
  */
case class registerCometActor(actor: BaseCometActor, name: Box[String])
case class unregisterCometActor(actor: BaseCometActor)
case class CometName(name: String)
