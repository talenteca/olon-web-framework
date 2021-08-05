package olon
package mongodb

import util.{ConnectionIdentifier, DefaultConnectionIdentifier}

import org.specs2.mutable.Specification
import org.specs2.execute.Result

import com.mongodb._

class MongoSpec extends Specification  {
  "Mongo Specification".title

  case object TestMongoIdentifier extends ConnectionIdentifier {
    val jndiName = "test_a"
  }

  def passDefinitionTests(id: ConnectionIdentifier, mc: MongoClient, db: String): Result = {
    // define the db
    MongoDB.defineDb(id, mc, db)

    // make sure mongo is running
    try {
      // this will throw an exception if it can't connect to the db
      mc.listDatabaseNames()
    } catch {
      case _: MongoTimeoutException =>
        skipped("MongoDB is not running")
    }

    // using an undefined identifier throws an exception
    MongoDB.useDatabase(DefaultConnectionIdentifier) { db =>
      db.listCollections
    } must throwA(new MongoException("Mongo not found: ConnectionIdentifier(lift)"))

    // remove defined db
    MongoDB.remove(id)

    success
  }

  "Mongo" should {

    "Define DB with MongoClient instance" in {
      val opts = MongoClientOptions.builder
        .connectionsPerHost(12)
        .serverSelectionTimeout(2000)
        .build
      passDefinitionTests(TestMongoIdentifier, new MongoClient(new ServerAddress("localhost"), opts), "test_default_b")
    }
  }
}
