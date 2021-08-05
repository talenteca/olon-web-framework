package olon
package mongodb
package record
package field

import olon.common.{Box, Empty, Failure, Full}
import olon.http.js.JE.{JsNull, JsRaw}
import olon.json._
import com.mongodb.DBObject

/**
* Describes common aspects related to Mongo fields
*/
@deprecated("Please use 'BsonableField' instead.", "3.4.3")
trait MongoFieldFlavor[MyType] {

  /*
  * convert this field's value into a DBObject so it can be stored in Mongo.
  */
  @deprecated("This was replaced with the functions from 'BsonableField'.", "3.4.3")
  def asDBObject: DBObject

  // set this field's value using a DBObject returned from Mongo.
  @deprecated("This was replaced with the functions from 'BsonableField'.", "3.4.3")
  def setFromDBObject(obj: DBObject): Box[MyType]

  /**
  * Returns the field's value as a valid JavaScript expression
  */
  def asJs = asJValue match {
    case JNothing => JsNull
    case jv => JsRaw(compactRender(jv))
  }

  /** Encode the field value into a JValue */
  def asJValue: JValue

}
