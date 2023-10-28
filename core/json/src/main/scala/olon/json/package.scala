package olon

package object json {

  type JValue = JsonAST.JValue
  val JNothing = JsonAST.JNothing
  val JNull = JsonAST.JNull
  type JString = JsonAST.JString
  val JString = JsonAST.JString
  type JDouble = JsonAST.JDouble
  val JDouble = JsonAST.JDouble
  type JInt = JsonAST.JInt
  val JInt = JsonAST.JInt
  type JBool = JsonAST.JBool
  val JBool = JsonAST.JBool
  type JField = JsonAST.JField
  val JField = JsonAST.JField
  type JObject = JsonAST.JObject
  val JObject = JsonAST.JObject
  type JArray = JsonAST.JArray
  val JArray = JsonAST.JArray

  def parse(s: String): JValue = JsonParser.parse(s)
  def parseOpt(s: String): Option[JValue] = JsonParser.parseOpt(s)

  def prettyRender(value: JValue): String = JsonAST.prettyRender(value)
  def compactRender(value: JValue): String = JsonAST.compactRender(value)
}
