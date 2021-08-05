package olon
package mongodb
package record

import org.bson.types.ObjectId

/**
  * Extend this to create extractors for your MongoRecords.
  *
  * Example:
  *    object AsUser extends AsMongoRecord(User)
  */
class AsMongoRecord[A <: MongoRecord[A]](meta: MongoMetaRecord[A]) {

  def unapply(in: String): Option[A] = asMongoRecord(in)

  def asMongoRecord(in: String): Option[A] =
    if (ObjectId.isValid(in)) meta.find(new ObjectId(in))
    else None
}
