package olon
package webapptest
package snippet

import olon.common.Loggable
import olon.http._

import scala.xml._

object Counter {
  @volatile var x = 0
}

class Oneshot extends Loggable {
  def render(in: NodeSeq): NodeSeq = {
    logger.trace("One shot with " + in.size + " nodes")
    S.disableTestFuncNames {
      S.oneShot {
        SHtml.text("", _ => { Counter.x += 1 })
      }
    }
  }
}

class Twoshot extends Loggable {
  def render(in: NodeSeq): NodeSeq = {
    logger.trace("Two shot with " + in.size + " nodes")
    S.disableTestFuncNames {
      SHtml.text("", _ => Counter.x += 1)
    }
  }
}
