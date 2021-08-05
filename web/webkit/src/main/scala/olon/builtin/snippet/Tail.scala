package olon 
package builtin 
package snippet 

import http._
import util._
import scala.xml._



object Tail extends DispatchSnippet {
   def dispatch: DispatchIt = {
     case _ => render _
   }

   def render(xhtml: NodeSeq) : NodeSeq = <tail>{xhtml}</tail>
}

/**
 * The 'head' snippet.  Use this snippet to move
 * a chunk of 
 */
object Head extends DispatchSnippet {
  lazy val valid = Set("title", "base",
                       "link", "meta", "style",
                       "script")

   def dispatch: DispatchIt = {
     case _ => render _
   }

   def render(_xhtml: NodeSeq) : NodeSeq = {
     def validHeadTagsOnly(in: NodeSeq): NodeSeq = 
       in  flatMap {
         case Group(ns) => validHeadTagsOnly(ns)
         case e: Elem if (null eq e.prefix) && valid.contains(e.label) => {
           new Elem(e.prefix,
                    e.label,
                    e.attributes,
                    e.scope,
                    e.minimizeEmpty,
                    validHeadTagsOnly(e.child) :_*)
         }
         case e: Elem if (null eq e.prefix) => NodeSeq.Empty
         case x => x
       }
     
       val xhtml = validHeadTagsOnly(_xhtml)

     <head>{
       if ((S.attr("withResourceId") or S.attr("withresourceid")).filter(Helpers.toBoolean).isDefined) {
         WithResourceId.render(xhtml)
       } else {
         xhtml
       }
     }</head>
   }
}
