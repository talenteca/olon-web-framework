package olon
package mongodb

import json._
import JsonDSL._
import util.Helpers._

import org.bson.types.ObjectId
import org.specs2.mutable.Specification

import org.bson._

class BsonParserSpec extends Specification  {
  "BsonParser Specification".title

  def buildTestData: (ObjectId, BsonDocument) = {
    val oid = ObjectId.get
    val dbo = BsonParser.parse(("x" -> oid.toString))(DefaultFormats)
    (oid, dbo)
  }

  "BsonParser" should {
    "convert strings to ObjectId by default" in {
      val (oid, dbo) = buildTestData
      val xval = tryo(dbo.getObjectId("x"))

      xval.toList map { x =>
        x.getValue must_== oid
      }

      xval.isDefined must_== true
    }
    "not convert strings to ObjectId when configured not to" in {
      BsonParser.stringProcessor.doWith((s: String) => new BsonString(s)) {
        val (oid, dbo) = buildTestData
        val xval = tryo(dbo.getString("x"))

        xval.toList map { x =>
          x.getValue must_== oid.toString
        }

        xval.isDefined must_== true
      }
    }
  }
}
