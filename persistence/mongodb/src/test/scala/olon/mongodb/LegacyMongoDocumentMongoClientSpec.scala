package olon
package mongodb

import BsonDSL._

import java.util.{Calendar, Date, UUID}
import java.util.regex.Pattern

import org.bson.types.ObjectId
import com.mongodb._

import org.specs2.mutable.Specification

import json._

package legacymongoclienttestdocs {
  case class SessCollection(_id: ObjectId, name: String, dbtype: String, count: Int)
    extends MongoDocument[SessCollection] {

    def meta = SessCollection
  }

  object SessCollection extends MongoDocumentMeta[SessCollection] {
    override def formats = super.formats + new ObjectIdSerializer
    // create a unique index on name
    createIndex(("name" -> 1), true)
  }
}

/**
 * Systems under specification for MongoDocumentMongoClient.
 */
class LegacyMongoDocumentMongoClientSpec extends Specification with MongoTestKit {
  "LegacyMongoDocumentMongoClient Specification".title

  import legacymongoclienttestdocs._

  "MongoClient example" in {

    checkMongoIsRunning

    val tc = SessCollection(ObjectId.get, "MongoSession", "db", 1)
    val tc2 = SessCollection(ObjectId.get, "MongoSession", "db", 1)
    val tc3 = SessCollection(ObjectId.get, "MongoDB", "db", 1)

    // save to db
    SessCollection.save(tc)
    SessCollection.save(tc2) must throwA[MongoException]
    SessCollection.save(tc3)

    success
  }

}
