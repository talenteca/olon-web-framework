package olon
package mongodb

import json._

import scala.util.matching.Regex
import java.util.{Date, UUID}
import java.util.regex.Pattern

import org.bson.types.ObjectId
import org.joda.time.DateTime

object BsonDSL extends JsonDSL {
  implicit def objectid2jvalue(oid: ObjectId): JValue = JsonObjectId(oid)
  implicit def pattern2jvalue(p: Pattern): JValue = JsonRegex(p)
  implicit def regex2jvalue(r: Regex): JValue = JsonRegex(r.pattern)
  implicit def uuid2jvalue(u: UUID): JValue = JsonUUID(u)
  implicit def date2jvalue(d: Date)(implicit formats: Formats): JValue = JsonDate(d)
  implicit def datetime2jvalue(d: DateTime)(implicit formats: Formats): JValue = JsonDateTime(d)
}
