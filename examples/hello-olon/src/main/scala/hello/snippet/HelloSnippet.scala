package hello.snippet 

import olon.util.Helpers._

import java.util.Date

class HelloSnippet {

  def render = {
    val now = new Date()
    val greet = "Hello Olon at the server now is " + now
    "*" #> greet
  }

}

