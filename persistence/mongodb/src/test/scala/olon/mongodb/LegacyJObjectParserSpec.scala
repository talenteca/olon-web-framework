package olon
package mongodb

import json._
import JsonDSL._
import util.Helpers._

import org.bson.types.ObjectId
import org.specs2.mutable.Specification

import com.mongodb.DBObject

class LegacyJObjectParserSpec extends Specification  {
  "LegacyJObjectParser Specification".title

  def buildTestData: (ObjectId, DBObject) = {
    val oid = ObjectId.get
    val dbo = JObjectParser.parse(("x" -> oid.toString))(DefaultFormats)
    (oid, dbo)
  }

  "JObjectParser" should {
    "convert strings to ObjectId by default" in {
      val (oid, dbo) = buildTestData
      val xval = tryo(dbo.get("x").asInstanceOf[ObjectId])

      xval.toList map { x =>
        x must_== oid
      }

      xval.isDefined must_== true
    }
    "not convert strings to ObjectId when configured not to" in {
      JObjectParser.stringProcessor.doWith((s: String) => s) {
        val (oid, dbo) = buildTestData
        val xval = tryo(dbo.get("x").asInstanceOf[String])

        xval.toList map { x =>
          x must_== oid.toString
        }

        xval.isDefined must_== true
      }
    }
  }
}
