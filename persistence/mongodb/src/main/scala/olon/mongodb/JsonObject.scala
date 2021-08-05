package olon
package mongodb

import json.Formats
import json.JsonAST.JObject

import scala.reflect.Manifest

import org.bson.types.ObjectId

/*
* These traits provide lift-json related convenience methods for case classes
* and their companion objects. Used by MongoDocument, JsonObjectField, and
* JsonObjectListField
*/
trait JsonObject[BaseDocument] {
  self: BaseDocument =>

  def meta: JsonObjectMeta[BaseDocument]

  // convert class to a json value
  def asJObject()(implicit formats: Formats): JObject = meta.toJObject(this)

}

class JsonObjectMeta[BaseDocument](implicit mf: Manifest[BaseDocument]) {

  import olon.json.Extraction._

  // create an instance of BaseDocument from a JObject
  def create(in: JObject)(implicit formats: Formats): BaseDocument =
    extract(in)(formats, mf)

  // convert class to a JObject
  def toJObject(in: BaseDocument)(implicit formats: Formats): JObject =
    decompose(in)(formats).asInstanceOf[JObject]
}

/*
* Case class for a db reference (foreign key). To be used in a JsonObject
* ref = collection name, id is the value of the reference
* Only works with ObjectIds.
*/
case class MongoRef(ref: String, id: ObjectId)

