package olon
package mongodb

import util.{ConnectionIdentifier, SimpleInjector}
import util.Helpers._

import com.mongodb.WriteConcern

object MongoRules extends SimpleInjector {
  private def defaultCollectionNameFunc(conn: ConnectionIdentifier, name: String): String = {
    charSplit(name, '.').last.toLowerCase+"s"
  }

  /**
    * Calculate the name of a collection based on the full
    * class name of the MongoDocument/MongoRecord. Must be
    * set in Boot before any code that touches the
    * MongoDocumentMeta/MongoMetaRecord.
    *
    * To get snake_case, use this
    *
    *  RecordRules.collectionName.default.set((_,name) => StringHelpers.snakify(name))
    */
  val collectionName = new Inject[(ConnectionIdentifier, String) => String](defaultCollectionNameFunc _) {}

  /** The default WriteConcern used in some places.
    */
  val defaultWriteConcern = new Inject[WriteConcern](WriteConcern.ACKNOWLEDGED) {}
}
