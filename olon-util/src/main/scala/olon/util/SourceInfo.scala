// SCALA3 these traits are not used, considere to remove them instead of migrate
// them
package olon.util

import olon.common.Box
import olon.json.JsonAST.JValue

import scala.reflect.runtime.universe._
import scala.xml.NodeSeq

/** A trait that allows an object to tell you about itself rather than using
  * reflection
  */
trait SourceInfo {

  /** Given a name, look up the field
    * @param name
    *   the name of the field
    * @return
    *   the metadata
    */
  def findSourceField(name: String): Box[SourceFieldInfo]

  /** Get a list of all the fields
    * @return
    *   a list of all the fields
    */
  def allFieldNames(): Seq[(String, SourceFieldMetadata)]
}

case class SourceFieldMetadataRep[A](
    name: String,
    manifest: TypeTag[A],
    converter: FieldConverter { type T = A }
) extends SourceFieldMetadata {
  type ST = A
}

/** Metadata about a specific field
  */
trait SourceFieldMetadata {

  /** The field's type
    */
  type ST

  /** The fields name
    * @return
    *   the field's name
    */
  def name: String

  /** The field's manifest
    * @return
    *   the field's manifest
    */
  def manifest: TypeTag[ST]

  /** Something that will convert the field into known types like String and
    * NodeSeq
    * @return
    */
  def converter: FieldConverter { type T = ST }
}

/** An inplementation of SourceFieldInfo
  *
  * @param value
  *   the value
  * @param metaData
  *   the metadata
  * @tparam A
  *   the type
  */
case class SourceFieldInfoRep[A](
    value: A,
    metaData: SourceFieldMetadata { type ST = A }
) extends SourceFieldInfo {
  type T = A
}

/** Value and metadata for a field
  */
trait SourceFieldInfo {

  /** The type of the field
    */
  type T

  /** The field's value
    * @return
    */
  def value: T

  /** Metadata about the field
    * @return
    */
  def metaData: SourceFieldMetadata { type ST = T }
}

/** Convert the field into other representations
  */
trait FieldConverter {

  /** The type of the field
    */
  type T

  /** Convert the field to a String
    * @param v
    *   the field value
    * @return
    *   the string representation of the field value
    */
  def asString(v: T): String

  /** Convert the field into NodeSeq, if possible
    * @param v
    *   the field value
    * @return
    *   a NodeSeq if the field can be represented as one
    */
  def asNodeSeq(v: T): Box[NodeSeq]

  /** Convert the field into a JSON value
    * @param v
    *   the field value
    * @return
    *   the JSON representation of the field
    */
  // SCALA3 Using internal type `T` for `JValue` type parameter
  def asJson(v: T): Box[JValue[T]]

  /** If the field can represent a sequence of SourceFields, get that
    * @param v
    *   the field value
    * @return
    *   the field as a sequence of SourceFields
    */
  def asSeq(v: T): Box[Seq[SourceFieldInfo]]
}
