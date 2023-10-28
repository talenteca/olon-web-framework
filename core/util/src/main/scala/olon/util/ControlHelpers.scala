package olon
package util

import common._

object ControlHelpers extends ControlHelpers with ClassHelpers

/** Control helpers provide alternate ways to catch exceptions and ignore them
  * as necessary
  */
trait ControlHelpers extends ClassHelpers with Tryo
