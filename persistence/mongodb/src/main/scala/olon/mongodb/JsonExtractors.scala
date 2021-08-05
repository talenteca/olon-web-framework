package olon
package mongodb

import json._
import util.Helpers.tryo
import JsonDSL._

import java.util.{Date, UUID}
import java.util.regex.Pattern

import org.bson.types.ObjectId
import org.joda.time.DateTime

object JsonObjectId {
  def unapply(json: JValue): Option[ObjectId] = {
    json match {
      case JObject(JField("$oid", JString(objectIdString)) :: Nil) if ObjectId.isValid(objectIdString) =>
        Some(new ObjectId(objectIdString))
      case _ =>
        None
    }
  }

  def apply(objectId: ObjectId): JValue = ("$oid" -> objectId.toString)

  def asJValue(objectId: ObjectId, formats: Formats): JValue =
    if (isObjectIdSerializerUsed(formats))
      apply(objectId)
    else
      JString(objectId.toString)

  /**
    * Check to see if the ObjectIdSerializer is being used.
    */
  private def isObjectIdSerializerUsed(formats: Formats): Boolean =
    formats.customSerializers.exists(_.getClass == objectIdSerializerClass)

  private val objectIdSerializerClass = classOf[olon.mongodb.ObjectIdSerializer]
}

object JsonRegex {
  def unapply(json: JValue): Option[Pattern] = {
    json match {
      case JObject(JField("$regex", JString(regex)) :: JField("$flags", JInt(f)) :: Nil) =>
        Some(Pattern.compile(regex, f.intValue))
      case _ =>
        None
    }
  }

  def apply(p: Pattern): JValue = ("$regex" -> p.pattern) ~ ("$flags" -> p.flags)
}

object JsonUUID {
  def unapply(json: JValue): Option[UUID] = {
    json match {
      case JObject(JField("$uuid", JString(s)) :: Nil) =>
        tryo(UUID.fromString(s))
      case _ =>
        None
    }
  }

  def apply(uuid: UUID): JValue = ("$uuid" -> uuid.toString)
}

object JsonDate {
  def unapply(json: JValue)(implicit formats: Formats): Option[Date] = {
    json match {
      case JObject(JField("$dt", JString(s)) :: Nil) =>
        formats.dateFormat.parse(s)
      case _ =>
        None
    }
  }

  def apply(dt: Date)(implicit formats: Formats): JValue = ("$dt" -> formats.dateFormat.format(dt))
  def apply(dt: Long)(implicit formats: Formats): JValue = ("$dt" -> formats.dateFormat.format(new Date(dt)))
}

object JsonDateTime {
  def unapply(json: JValue)(implicit formats: Formats): Option[DateTime] = {
    json match {
      case JObject(JField("$dt", JString(s)) :: Nil) =>
        formats.dateFormat.parse(s).map(dt => new DateTime(dt))
      case _ =>
        None
    }
  }

  def apply(dt: DateTime)(implicit formats: Formats): JValue = ("$dt" -> formats.dateFormat.format(dt.toDate))
}
