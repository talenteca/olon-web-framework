package olon
package mongodb

import common._
import util.Helpers._

import org.specs2.mutable._

import org.bson.types.ObjectId

case class CollectionNameTestDoc(_id: ObjectId) extends MongoDocument[CollectionNameTestDoc] {
  def meta = CollectionNameTestDoc
}
object CollectionNameTestDoc extends MongoDocumentMeta[CollectionNameTestDoc]

/**
  * Systems under specification for MongoRules.
  */
class MongoRulesSpec extends Specification {
  "Mongo Rules Specification".title
  sequential

  "MongoRules" should {
    "default collection name" in {
      CollectionNameTestDoc.collectionName must_== "collectionnametestdocs"
    }
    "snakify collection name" in {
      MongoRules.collectionName.doWith((_, name) => snakify(name)+"s") {
        CollectionNameTestDoc.collectionName must_== "olon.mongodb.collection_name_test_docs"
      }
    }
  }
}
