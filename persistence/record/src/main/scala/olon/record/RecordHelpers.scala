package olon
package record

import olon.http.js.{JsExp, JsObj}
import olon.http.js.JE.{JsArray, JsFalse, JsNull, JsObj, JsTrue, Num, Str}
import olon.json.JsonAST.{JArray, JBool, JInt, JDouble, JField, JNothing, JNull, JObject, JString, JValue}

object RecordHelpers {
  
  /* For the moment, I couldn't find any other way to bridge JValue and JsExp, so I wrote something simple here */
  implicit def jvalueToJsExp(jvalue: JValue): JsExp = {
    jvalue match {
      case JArray(vs)  => JsArray(vs.map(jvalueToJsExp): _*)
      case JBool(b)    => if (b) JsTrue else JsFalse
      case JDouble(d)  => Num(d)
      case JInt(i)     => Num(i)
      case JNothing    => JsNull
      case JNull       => JsNull
      case JObject(fs) => JsObj(fs.map(f => (f.name, jvalueToJsExp(f.value))): _*)
      case JString(s)  => Str(s)
    }
  }
}

