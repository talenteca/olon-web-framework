package olon
package webapptest
package snippet

import olon.http._

import scala.xml._

object Counter {
  @volatile var x = 0
}

class Oneshot {
  def render(in: NodeSeq): NodeSeq = {
    S.disableTestFuncNames {
      S.oneShot {
        SHtml.text("", s => { Counter.x += 1 })
      }
    }
  }
}

class Twoshot {
  def render(in: NodeSeq): NodeSeq = {
    S.disableTestFuncNames {
      SHtml.text("", s => Counter.x += 1)
    }
  }
}
