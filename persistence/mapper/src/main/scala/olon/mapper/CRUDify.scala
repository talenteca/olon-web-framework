package olon
package mapper

import util._
import common._

import scala.xml._

/**
 * This trait automatically adds CRUD (Create, read, update and delete) operations
 * to an existing <b>MetaMapper</b> object. Various methods can be overridden to
 * customize which operations are available to a user and how things are displayed.
 * For example, you can disable deletion of entities by overriding deleteMenuLoc to Empty.
 *
 * Note: Compilation will fail if you try to mix this into a Mapper instead of the
 * associated MetaMapper. You have been warned.
 */
trait CRUDify[KeyType, CrudType <: KeyedMapper[KeyType, CrudType]] extends 
  olon.proto.Crudify {
  self: CrudType with KeyedMetaMapper[KeyType, CrudType] =>

  /**
   * What's the record type for the underlying CRUDify?
   */
  type TheCrudType = CrudType

  /**
   * What's a field pointer for the underlying CRUDify
   */
  type FieldPointerType = MappedField[_, CrudType]

  /**
   * Given a field pointer and an instance, get the field on that instance
   */
  protected def computeFieldFromPointer(instance: TheCrudType, pointer: FieldPointerType): Box[BaseField] = Full(getActualField(instance, pointer))

  /**
   * Given a String that represents the primary key, find an instance of
   * TheCrudType
   */
  def findForParam(in: String): Box[TheCrudType] = find(in)

  /**
   * Get a List of items from the databased
   */
  def findForList(start: Long, count: Int): List[TheCrudType] =
  findAll(StartAt[CrudType](start) :: MaxRows[CrudType](count) ::
          findForListParams :_*)

  /**
   * What are the query parameters?  Default to ascending on primary key
   */
  def findForListParams: List[QueryParam[CrudType]] =
  List(OrderBy(primaryKeyField, Ascending))

  /**
  * The fields to be displayed. By default all the displayed fields,
  * but this list
  * can be shortened.
  */
  def fieldsForDisplay: List[MappedField[_, CrudType]] = 
    mappedFieldsForModel.filter(_.dbDisplay_?)

  /**
   * What's the prefix for this CRUD.  Typically the table name
   */
  def calcPrefix = List(_dbTableNameLC)


  protected class MyBridge(in: CrudType) extends CrudBridge {
    /**
     * Delete the instance of TheCrudType from the backing store
     */
    def delete_! : Boolean = in.delete_!

    /**
     * Save an instance of TheCrudType in backing store
     */
    def save : Boolean = in.save

    /**
     * Validate the fields in TheCrudType and return a List[FieldError]
     * representing the errors.
     */
    def validate: List[FieldError] = in.validate

    /**
     * Return a string representation of the primary key field
     */
    def primaryKeyFieldAsString: String = in.primaryKeyField.toString
  }

  /**
   * This method will instantiate a bridge from TheCrudType so
   * that the appropriate logical operations can be performed
   * on TheCrudType
   */
  protected implicit def buildBridge(from: TheCrudType): CrudBridge =
    new MyBridge(from)

  protected class MyPointer(in: MappedField[_, CrudType]) extends FieldPointerBridge {
    /**
     * What is the display name of this field?
     */
    def displayHtml: NodeSeq = in.displayHtml
  }

  /**
   * Based on a FieldPointer, build a FieldPointerBridge
   */
  protected implicit def buildFieldBridge(from: FieldPointerType): FieldPointerBridge = new MyPointer(from)


}


/**
 * A specialization of CRUDify for LongKeyedMetaMappers.
 */
trait LongCRUDify[CrudType <: KeyedMapper[Long, CrudType]] extends CRUDify[Long, CrudType] {
  self: CrudType with KeyedMetaMapper[Long, CrudType] =>
}

