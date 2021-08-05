package olon.mongodb
package codecs

import org.bson.BsonType

/**
 * A companion object for BsonTypeClassMap.
 */
object BsonTypeClassMap {
  def apply(replacements: (BsonType, Class[_])*): BsonTypeClassMap = {
    val jreplacements = new java.util.HashMap[BsonType, Class[_]]()
    replacements.foreach(kv => jreplacements.put(kv._1, kv._2))
    new BsonTypeClassMap(jreplacements)
  }
}