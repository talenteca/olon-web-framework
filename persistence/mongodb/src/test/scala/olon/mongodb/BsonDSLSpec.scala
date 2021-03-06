package olon
package mongodb

import BsonDSL._
import json._

import scala.collection.JavaConverters._
import scala.util.matching.Regex

import java.util.{Date, UUID}
import java.util.regex.Pattern

import org.bson.types.ObjectId
import org.joda.time.DateTime
import org.specs2.mutable.Specification

import com.mongodb.{BasicDBList, DBObject}

class BsonDSLSpec extends Specification  {
  "BsonDSL Specification".title

  "BsonDSL" should {
    "Convert ObjectId properly" in {
      val oid: ObjectId = ObjectId.get
      val qry: JObject = ("id" -> oid)
      val dbo: DBObject = JObjectParser.parse(qry)(DefaultFormats)

      dbo.get("id") must_== oid
    }

    "Convert List[ObjectId] properly" in {
      val oidList = ObjectId.get :: ObjectId.get :: ObjectId.get :: Nil
      val qry: JObject = ("ids" -> oidList)
      val dbo: DBObject = JObjectParser.parse(qry)(DefaultFormats)
      val oidList2: List[ObjectId] =
        dbo
          .get("ids")
          .asInstanceOf[BasicDBList]
          .asScala
          .toList
          .map(_.asInstanceOf[ObjectId])

      oidList2 must_== oidList
    }

    "Convert Pattern properly" in {
      val ptrn: Pattern = Pattern.compile("^Mongo", Pattern.MULTILINE | Pattern.CASE_INSENSITIVE)
      val qry: JObject = ("ptrn" -> ptrn)
      val dbo: DBObject = JObjectParser.parse(qry)(DefaultFormats)
      val ptrn2: Pattern = dbo.get("ptrn").asInstanceOf[Pattern]

      ptrn2.pattern must_== ptrn.pattern
      ptrn2.flags must_== ptrn.flags
    }

    "Convert List[Pattern] properly" in {
      val ptrnList =
        Pattern.compile("^Mongo1", Pattern.MULTILINE | Pattern.CASE_INSENSITIVE) ::
        Pattern.compile("^Mongo2", Pattern.CASE_INSENSITIVE) ::
        Pattern.compile("^Mongo3") :: Nil
      val qry: JObject = ("ptrns" -> ptrnList)
      val dbo: DBObject = JObjectParser.parse(qry)(DefaultFormats)
      val ptrnList2: List[Pattern] =
        dbo
          .get("ptrns")
          .asInstanceOf[BasicDBList]
          .asScala
          .toList
          .map(_.asInstanceOf[Pattern])

      for (i <- 0 to 2) yield {
        ptrnList(i).pattern must_== ptrnList2(i).pattern
        ptrnList(i).flags must_== ptrnList2(i).flags
      }

      ptrnList2.length must_== ptrnList.length
    }

    "Convert Regex properly" in {
      val regex: Regex = "^Mongo".r
      val qry: JObject = ("regex" -> regex)
      val dbo: DBObject = JObjectParser.parse(qry)(DefaultFormats)
      val ptrn: Pattern = dbo.get("regex").asInstanceOf[Pattern]

      regex.pattern.pattern must_== ptrn.pattern
      regex.pattern.flags must_== ptrn.flags
    }

    "Convert UUID properly" in {
      val uuid: UUID = UUID.randomUUID
      val qry: JObject = ("uuid" -> uuid)
      val dbo: DBObject = JObjectParser.parse(qry)(DefaultFormats)

      dbo.get("uuid") must_== uuid
    }

    "Convert List[UUID] properly" in {
      val uuidList = UUID.randomUUID :: UUID.randomUUID :: UUID.randomUUID :: Nil
      val qry: JObject = ("ids" -> uuidList)
      val dbo: DBObject = JObjectParser.parse(qry)(DefaultFormats)
      val uuidList2: List[UUID] =
        dbo
          .get("ids")
          .asInstanceOf[BasicDBList]
          .asScala
          .toList
          .map(_.asInstanceOf[UUID])

      uuidList2 must_== uuidList
    }

    "Convert Date properly" in {
      implicit val formats = DefaultFormats.lossless
      val dt: Date = new Date
      val qry: JObject = ("now" -> dt)
      val dbo: DBObject = JObjectParser.parse(qry)

      dbo.get("now") must_== dt
    }

    "Convert List[Date] properly" in {
      implicit val formats = DefaultFormats.lossless
      val dateList = new Date :: new Date :: new Date :: Nil
      val qry: JObject = ("dts" -> dateList)
      val dbo: DBObject = JObjectParser.parse(qry)
      val dateList2: List[Date] =
        dbo
          .get("dts")
          .asInstanceOf[BasicDBList]
          .asScala
          .toList
          .map(_.asInstanceOf[Date])

      dateList2 must_== dateList
    }

    "Convert DateTime properly" in {
      implicit val formats = DefaultFormats.lossless
      val dt: DateTime = new DateTime
      val qry: JObject = ("now" -> dt)
      val dbo: DBObject = JObjectParser.parse(qry)

      new DateTime(dbo.get("now")) must_== dt
    }

    "Convert List[DateTime] properly" in {
      implicit val formats = DefaultFormats.lossless
      val dateList = new DateTime :: new DateTime :: new DateTime :: Nil
      val qry: JObject = ("dts" -> dateList)
      val dbo: DBObject = JObjectParser.parse(qry)
      val dateList2: List[DateTime] =
        dbo
          .get("dts")
          .asInstanceOf[BasicDBList]
          .asScala
          .toList
          .map(_.asInstanceOf[Date]).map(d => new DateTime(d))

      dateList2 must_== dateList
    }
  }
}