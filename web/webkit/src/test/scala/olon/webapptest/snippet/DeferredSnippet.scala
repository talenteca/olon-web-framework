package olon 
package webapptest 
package snippet 

import olon.http._
import scala.xml._

class DeferredSnippet {
  object MyNumber extends RequestVar(55)

  def first:NodeSeq = {
    MyNumber.set(44)
    <span id="first">first</span>
  }

  def secondLazy:NodeSeq = {
    val old = MyNumber.is
    MyNumber.set(99)
    <span id="second">Very lazy {old}</span>
  }

  def third:NodeSeq = {
    <span id="third">third {MyNumber.is}</span>
  }

   def stackWhack: NodeSeq = {
     val inActor: Boolean = Thread.currentThread.getStackTrace.exists(_.getClassName.contains("olon.actor."))

     <span id={"actor_"+inActor}>stackWhack</span>
   }
}

