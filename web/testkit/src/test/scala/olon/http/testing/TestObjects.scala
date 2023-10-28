package olon
package http
package testing

import olon.common.Box

/*
 * The purpose of these classes is not to run actual tests,
 * but to insure that tests can be run correctly by
 * making sure they compile correctly
 */

object MyCode extends TestKit {
  val baseUrl = ""

  val l2: TestResponse = post("/foo")
  l2.foreach { x: HttpResponse =>
    val l3: TestResponse = x.get("ddd")
    println("Hello")
  }

  for {
    login <- post("/whatever")
    next <- login.get("/bla")
  } {}
}

object MyBoxCode extends RequestKit {
  def baseUrl = ""

  val l2: Box[TheResponse] = post("/foo")
  l2.foreach { x: TheResponse =>
    val l3: Box[TheResponse] = x.get("ddd")
    println("Hello")
  }

  for {
    login: TheResponse <- post("/whatever")
    next <- login.get("/bla")
  } {}

}
