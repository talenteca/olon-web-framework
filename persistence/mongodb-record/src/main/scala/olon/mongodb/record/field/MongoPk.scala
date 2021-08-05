package olon
package mongodb
package record
package field

import common.{Box, Empty, Full}
import util.StringHelpers

import scala.util.Random
import java.util.UUID

import org.bson.types.ObjectId
import olon.record.field.{IntField, LongField, StringField}

/*
 * Trait for creating a "Primary Key" Field. These are all an id field
 * that is saved as _id in the database. Mix one of these into your
 * MongoRecord.
 */
trait MongoPk[PkType] {
  def id: PkType
  /** Override this to set default value of id field */
  def defaultIdValue: Any
}

trait ObjectIdPk[OwnerType <: MongoRecord[OwnerType]]
  extends MongoPk[ObjectIdField[OwnerType]]
{
  self: OwnerType =>

  def defaultIdValue = ObjectId.get

  object id extends ObjectIdField(this.asInstanceOf[OwnerType]) {
    override def name = "_id"
    override def defaultValue = defaultIdValue
    override def shouldDisplay_? = false
  }
}

trait UUIDPk[OwnerType <: MongoRecord[OwnerType]]
  extends MongoPk[UUIDField[OwnerType]]
{
  self: OwnerType =>

  def defaultIdValue = UUID.randomUUID

  object id extends UUIDField(this.asInstanceOf[OwnerType]) {
    override def name = "_id"
    override def defaultValue = defaultIdValue
    override def shouldDisplay_? = false
  }
}

trait StringPk[OwnerType <: MongoRecord[OwnerType]]
  extends MongoPk[StringField[OwnerType]]
{
  self: OwnerType =>

  def defaultIdValue = StringHelpers.randomString(maxIdLength)
  def maxIdLength: Int = 32

  object id extends StringField(this.asInstanceOf[OwnerType], maxIdLength) {
    override def name = "_id"
    override def defaultValue = defaultIdValue
    override def shouldDisplay_? = false
  }
}

trait IntPk[OwnerType <: MongoRecord[OwnerType]]
  extends MongoPk[IntField[OwnerType]]
{
  self: OwnerType =>

  def defaultIdValue = Random.nextInt

  object id extends IntField(this.asInstanceOf[OwnerType]) {
    override def name = "_id"
    override def defaultValue = defaultIdValue
    override def shouldDisplay_? = false
  }
}

trait LongPk[OwnerType <: MongoRecord[OwnerType]]
  extends MongoPk[LongField[OwnerType]]
{
  self: OwnerType =>

  def defaultIdValue = Random.nextLong

  object id extends LongField(this.asInstanceOf[OwnerType]) {
    override def name = "_id"
    override def defaultValue = defaultIdValue
    override def shouldDisplay_? = false
  }
}
