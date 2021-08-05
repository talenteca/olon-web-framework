package olon
package mongodb

import org.bson.types.ObjectId

/**
  * An ObjectId extractor.
  */
object AsObjectId {
  def unapply(in: String): Option[ObjectId] = asObjectId(in)

  def asObjectId(in: String): Option[ObjectId] =
    if (ObjectId.isValid(in)) Some(new ObjectId(in))
    else None
}
