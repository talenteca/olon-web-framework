package olon
package mongodb
package record
package field

import common.{Box, Empty, Full}
import http.SHtml
import util.Helpers._

import java.util.UUID

import org.bson.types.ObjectId
import olon.record.TypedField
import olon.record.field._

/*
 * Trait for creating a Field for storing a "foreign key". Caches the
 * item after fetching. Implementations are available for ObjectId, UUID, String,
 * Int, and Long, but you can mix this into any Field.
 *
 * toForm produces a select form element. You just need to supply the
 * options by overriding the options method.
 */
trait MongoRefField[RefType <: MongoRecord[RefType], MyType] extends TypedField[MyType] {

  /** The MongoMetaRecord of the referenced object **/
  def refMeta: MongoMetaRecord[RefType]

  /**
    * Find the referenced object
    */
  def find: Box[RefType] = valueBox.flatMap(v => refMeta.findAny(v))

  /**
    * Get the cacheable referenced object
    */
  def obj = synchronized {
    if (!_calcedObj) {
      _calcedObj = true
      this._obj = find
    }
    _obj
  }

  def cached_? : Boolean = synchronized { _calcedObj }

  def primeObj(obj: Box[RefType]) = synchronized {
    _obj = obj
    _calcedObj = true
  }

  private[this] var _obj: Box[RefType] = Empty
  private[this] var _calcedObj = false

  override def setBox(in: Box[MyType]): Box[MyType] = synchronized {
    _calcedObj = false // invalidate the cache
    super.setBox(in)
  }

  /** Options for select list **/
  def options: List[(Box[MyType], String)] = Nil

  /** Label for the selection item representing Empty, show when this field is optional. Defaults to the empty string. */
  def emptyOptionLabel: String = ""

  def buildDisplayList: List[(Box[MyType], String)] = {
    if (optional_?) (Empty, emptyOptionLabel)::options else options
  }

  private[this] def elem = SHtml.selectObj[Box[MyType]](
    buildDisplayList,
    Full(valueBox),
    setBox
  ) % ("tabindex" -> tabIndex.toString)

  override def toForm =
    if (options.nonEmpty) {
      uniqueFieldId match {
        case Full(id) => Full(elem % ("id" -> id))
        case _ => Full(elem)
      }
    } else {
      Empty
    }
}

class ObjectIdRefField[OwnerType <: BsonRecord[OwnerType], RefType <: MongoRecord[RefType]](
  @deprecatedName('rec) owner: OwnerType, val refMeta: MongoMetaRecord[RefType]
) extends ObjectIdField[OwnerType](owner) with MongoRefField[RefType, ObjectId] {
  override def find: Box[RefType] = valueBox.flatMap(v => refMeta.find(v))
}

class OptionalObjectIdRefField[OwnerType <: BsonRecord[OwnerType], RefType <: MongoRecord[RefType]](
  owner: OwnerType, val refMeta: MongoMetaRecord[RefType]
) extends OptionalObjectIdField[OwnerType](owner) with MongoRefField[RefType, ObjectId] {
  override def find: Box[RefType] = valueBox.flatMap(v => refMeta.find(v))
}


class UUIDRefField[OwnerType <: BsonRecord[OwnerType], RefType <: MongoRecord[RefType]](
  @deprecatedName('rec) owner: OwnerType, val refMeta: MongoMetaRecord[RefType]
) extends UUIDField[OwnerType](owner) with MongoRefField[RefType, UUID] {
  override def find: Box[RefType] = valueBox.flatMap(v => refMeta.find(v))
}

class OptionalUUIDRefField[OwnerType <: BsonRecord[OwnerType], RefType <: MongoRecord[RefType]](
  owner: OwnerType, val refMeta: MongoMetaRecord[RefType]
) extends OptionalUUIDField[OwnerType](owner) with MongoRefField[RefType, UUID] {
  override def find: Box[RefType] = valueBox.flatMap(v => refMeta.find(v))
}

class StringRefField[OwnerType <: BsonRecord[OwnerType], RefType <: MongoRecord[RefType]](
  @deprecatedName('rec) owner: OwnerType, val refMeta: MongoMetaRecord[RefType], maxLen: Int
) extends StringField[OwnerType](owner, maxLen) with MongoRefField[RefType, String] {
  override def find: Box[RefType] = valueBox.flatMap(v => refMeta.find(v))
}

class OptionalStringRefField[OwnerType <: BsonRecord[OwnerType], RefType <: MongoRecord[RefType]](
  owner: OwnerType, val refMeta: MongoMetaRecord[RefType], maxLen: Int
) extends OptionalStringField[OwnerType](owner, maxLen) with MongoRefField[RefType, String] {
  override def find: Box[RefType] = valueBox.flatMap(v => refMeta.find(v))
}

class IntRefField[OwnerType <: BsonRecord[OwnerType], RefType <: MongoRecord[RefType]](
  @deprecatedName('rec) owner: OwnerType, val refMeta: MongoMetaRecord[RefType]
) extends IntField[OwnerType](owner) with MongoRefField[RefType, Int] {
  override def find: Box[RefType] = valueBox.flatMap(v => refMeta.find(v))
}

class OptionalIntRefField[OwnerType <: BsonRecord[OwnerType], RefType <: MongoRecord[RefType]](
  owner: OwnerType, val refMeta: MongoMetaRecord[RefType]
) extends OptionalIntField[OwnerType](owner) with MongoRefField[RefType, Int] {
  override def find: Box[RefType] = valueBox.flatMap(v => refMeta.find(v))
}

class LongRefField[OwnerType <: BsonRecord[OwnerType], RefType <: MongoRecord[RefType]](
  @deprecatedName('rec) owner: OwnerType, val refMeta: MongoMetaRecord[RefType]
) extends LongField[OwnerType](owner) with MongoRefField[RefType, Long] {
  override def find: Box[RefType] = valueBox.flatMap(v => refMeta.find(v))
}

class OptionalLongRefField[OwnerType <: BsonRecord[OwnerType], RefType <: MongoRecord[RefType]](
  owner: OwnerType, val refMeta: MongoMetaRecord[RefType]
) extends OptionalLongField[OwnerType](owner) with MongoRefField[RefType, Long] {
  override def find: Box[RefType] = valueBox.flatMap(v => refMeta.find(v))
}
