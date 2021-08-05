package olon
package mongodb

import json.DefaultFormats

import com.mongodb._

import org.specs2.mutable.Specification

/**
 * System under specification for MongoDirectMonoClient.
 */
class LegacyMongoDirectMongoClientSpec extends Specification with MongoTestKit {
  "LegacyMongoDirectMongoClient Specification".title

  "MongoClient example" in {

    checkMongoIsRunning

    // use a Mongo instance directly
    MongoDB.use( db => {
      val coll = db.getCollection("testCollection")

      // create a unique index on name
      coll.createIndex(new BasicDBObject("name", 1), new BasicDBObject("unique", true))

      // build the DBObjects
      val doc = new BasicDBObject
      val doc2 = new BasicDBObject
      val doc3 = new BasicDBObject

      doc.put("name", "MongoSession")
      doc.put("type", "db")
      doc.put("count", 1: java.lang.Integer)

      doc2.put("name", "MongoSession")
      doc2.put("type", "db")
      doc2.put("count", 1: java.lang.Integer)

      doc3.put("name", "MongoDB")
      doc3.put("type", "db")
      doc3.put("count", 1: java.lang.Integer)

      // save the docs to the db
      coll.save(doc)
      coll.save(doc2) must throwA[MongoException]
      coll.save(doc3)
    })
    success
  }
}
