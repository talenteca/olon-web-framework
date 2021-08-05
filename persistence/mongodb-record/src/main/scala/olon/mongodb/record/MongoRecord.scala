package olon
package mongodb
package record

import olon.record.{MetaRecord, Record}
import olon.util.Helpers.tryo

import com.mongodb.{BasicDBObject, DBObject, DBRef, WriteConcern}

import org.bson.types.ObjectId
import common.{Full, Box}
import scala.concurrent.Future

trait MongoRecord[MyType <: MongoRecord[MyType]] extends BsonRecord[MyType] {
  self: MyType =>

  /**
   * Every MongoRecord must have an _id field. Use a MongoPkField to
   * satisfy this.
   *
   * This may change to type MandatoryTypedField in the
   * future (once MongoId is removed.)
   */
  def id: Any

  /**
   * The meta record (the object that contains the meta result for this type)
   */
  def meta: MongoMetaRecord[MyType]

  /**
   * Save the instance and return the instance
   */
  @deprecated("Set WriteConcern in MongoClientOptions or on the MongoMetaRecord", "3.4.3")
  def save(concern: WriteConcern): MyType = {
    runSafe {
      meta.save(this, concern)
    }
    this
  }

  /**
   * Inserts record and returns Future that completes when mongo driver finishes operation
   */
  @deprecated("No longer supported. This will be removed in Lift 4.", "3.4.3")
  def insertAsync():Future[Boolean] = {
    runSafe {
      meta.insertAsync(this)
    }
  }

  /**
   * Save the instance and return the instance
   */
  override def saveTheRecord(): Box[MyType] = saveBox()

  /**
   * Save the instance and return the instance
   * @param safe - if true will use WriteConcern ACKNOWLEDGED else UNACKNOWLEDGED
   */
  @deprecated("Set WriteConcern in MongoClientOptions or on the MongoMetaRecord", "3.4.3")
  def save(safe: Boolean = true): MyType = {
    save(if (safe) WriteConcern.ACKNOWLEDGED else WriteConcern.UNACKNOWLEDGED)
  }

  def save(): MyType = {
    runSafe {
      meta.save(this)
    }
    this
  }

  /**
   * Try to save the instance and return the instance in a Box.
   */
  def saveBox(): Box[MyType] = tryo {
    runSafe {
      meta.save(this)
    }
    this
  }

  /**
   * Update only the dirty fields
   */
  @deprecated("Use updateOne, or replaceOne instead", "3.4.3")
  def update: MyType = {
    runSafe {
      meta.update(this)
    }
    this
  }

  /**
   * Try to update only the dirty fields
   */
  @deprecated("Use updateOne, or replaceOne instead", "3.4.3")
  def updateBox: Box[MyType] = tryo {
    runSafe {
      meta.update(this)
    }
    this
  }

  /**
   * Delete the instance from backing store
   */
  def delete_! : Boolean = {
    runSafe {
      meta.delete_!(this)
    }
  }

  /**
   * Try to delete the instance from backing store
   */
  def deleteBox_! : Box[Boolean] = tryo {
    runSafe {
      meta.delete_!(this)
    }
  }
}
