package olon.json.scalaz

import olon.json._
import org.specs2.mutable.Specification
import scalaz._

import JsonScalaz._

object TupleExample extends Specification {
  "Parse tuple from List" in {
    val json = JsonParser.parse(""" [1,2,3] """)
    fromJSON[Tuple3[Int, Int, Int]](json) mustEqual Success(1, 2, 3)
  }
}
