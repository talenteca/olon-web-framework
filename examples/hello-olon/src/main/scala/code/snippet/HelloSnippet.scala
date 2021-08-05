package code 
package snippet 

import scala.xml.{NodeSeq, Text}
import olon.util._
import olon.common._
import java.util.Date
import Helpers._

class HelloSnippet {

  def render = {
    val now = new Date()
    val greet = "Hello Olon at the server now is " + now
    "*" #> greet
  }

}

