package olon
package builtin
package snippet


import olon.http._
import olon.common._
import olon.util._
import scala.xml._
import Helpers._

/**
 * Adds a resource id entity for each URI in order to control browser caching.
 * The rules of creating "unique" URI's are defined in LiftRules.attachResourceId function.
 * 
 * <pre>
 * &lt;lift:with-resource-id>
 *   &lt;link ... />
 *   &lt;script ... />
 * &lt;/lift:with-resource-id>
 * </pre>
 */
object WithResourceId extends DispatchSnippet {
  def dispatch: DispatchIt = {
    case _ =>  render
  }


  import Helpers._

  def render(xhtml: NodeSeq): NodeSeq = {
    xhtml flatMap (_ match {
     case e: Elem if e.label == "link" => 
        attrStr(e.attributes, "href").map { href =>
          e.copy(attributes =
            MetaData.update(e.attributes, 
                            e.scope,
                            new UnprefixedAttribute("href", LiftRules.attachResourceId(href), Null))
          )
        } openOr e
     case e: Elem if e.label == "script" => 
        attrStr(e.attributes, "src") map { src =>
          e.copy(attributes =
             MetaData.update(e.attributes, 
                             e.scope, 
                             new UnprefixedAttribute("src", LiftRules.attachResourceId(src), Null))
          )
        } openOr e
     case e => e
    })
  }


  private def attrStr(attrs: MetaData, attr: String): Box[String] = (attrs.get(attr) match {
    case None => Empty
    case Some(Nil) => Empty 
    case Some(x) => Full(x.toString)
  }) or (attrs.get(attr.toLowerCase) match {
    case None => Empty
    case Some(Nil) => Empty 
    case Some(x) => Full(x.toString)
  })
}

