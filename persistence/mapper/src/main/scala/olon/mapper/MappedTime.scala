package olon
package mapper

import java.sql.Types
import java.util.Date
import java.lang.reflect.Method

import util._
import common._
import Helpers._
import http._
import js._
import json._

import scala.xml.{Text, NodeSeq}

/**
 * Represents a time with hour, minute and second fields. The underlying type is
 * java.util.Date to keep things simple, but be aware that the date portion of the
 * values will most likely be discarded when this is saved to the database.
 *
 * @see MappedDateTime
 * @see MappedDate
 */

abstract class MappedTime[T<:Mapper[T]](val fieldOwner: T) extends MappedField[Date, T] {
  private val data = FatLazy(defaultValue)
  private val orgData = FatLazy(defaultValue)

  /**
   * This method defines the string parsing semantics of this field. Used in setFromAny.
   * By default uses LiftRules.dateTimeConverter's parseTime; override for field-specific behavior
   */
  def parse(s: String): Box[Date] = LiftRules.dateTimeConverter().parseTime(s)
  /**
   * This method defines the string parsing semantics of this field. Used in toString, _toForm.
   * By default uses LiftRules.dateTimeConverter's formatTime; override for field-specific behavior
   */
  def format(d: Date): String = LiftRules.dateTimeConverter().formatTime(d)


  import scala.reflect.runtime.universe._
  def manifest: TypeTag[Date] = typeTag[Date]

  /**
   * Get the source field metadata for the field
   * @return the source field metadata for the field
   */
  def sourceInfoMetadata(): SourceFieldMetadata{type ST = Date} =
    SourceFieldMetadataRep(name, manifest, new FieldConverter {
      /**
       * The type of the field
       */
      type T = Date

      /**
       * Convert the field to a String
       * @param v the field value
       * @return the string representation of the field value
       */
      def asString(v: T): String = format(v)

      /**
       * Convert the field into NodeSeq, if possible
       * @param v the field value
       * @return a NodeSeq if the field can be represented as one
       */
      def asNodeSeq(v: T): Box[NodeSeq] = Full(Text(asString(v)))

      /**
       * Convert the field into a JSON value
       * @param v the field value
       * @return the JSON representation of the field
       */
      def asJson(v: T): Box[JValue] = Full(JInt(v.getTime))

      /**
       * If the field can represent a sequence of SourceFields,
       * get that
       * @param v the field value
       * @return the field as a sequence of SourceFields
       */
      def asSeq(v: T): Box[Seq[SourceFieldInfo]] = Empty
    })

  protected def real_i_set_!(value: Date): Date = {
    if (value != data.get) {
      data() = value
      this.dirty_?( true)
    }
    data.get
  }

  def dbFieldClass = classOf[Date]

  def toLong: Long = get match {
    case null => 0L
    case d: Date => d.getTime / 1000L
  }

  def asJsExp: JsExp = JE.Num(toLong)

  def asJsonValue: Box[JsonAST.JValue] = Full(get match {
    case null => JsonAST.JNull
    case x => JsonAST.JInt(x.getTime)
  })

  /**
   * Get the JDBC SQL Type for this field
   */
  def targetSQLType = Types.TIME

  def defaultValue: Date = null
  // private val defaultValue_i = new Date

  override def writePermission_? = true
  override def readPermission_? = true

  protected def i_is_! = data.get
  protected def i_was_! = orgData.get
  protected[mapper] def doneWithSave(): Unit = {orgData.setFrom(data)}

  protected def i_obscure_!(in : Date) : Date = {
    new Date(0L)
  }

  /**
   * Create an input field for the item
   */
  override def _toForm: Box[NodeSeq] =
  S.fmapFunc({s: List[String] => this.setFromAny(s)}){funcName =>
  Full(appendFieldId(<input type={formInputType}
                     name={funcName}
                     value={get match {case null => "" case s => format(s)}}/>))
  }

  override def setFromAny(f : Any): Date = f match {
    case JsonAST.JNull => this.set(null)
    case JsonAST.JInt(v) => this.set(new Date(v.longValue))
    case "" | null => this.set(null)
    case s: String => parse(s).map(s => this.set(s)).openOr(this.get)
    case x :: _ => setFromAny(x)
    case d: Date => this.set(d)
    case Some(d: Date) => this.set(d)
    case Full(d: Date) => this.set(d)
    case None | Empty | Failure(_, _, _) => this.set(null)
    case f => toDate(f).map(d => this.set(d)).openOr(this.get)
  }

  def jdbcFriendly(field : String) : Object = get match {
    case null => null
    case d => new java.sql.Time(d.getTime)
  }

  def real_convertToJDBCFriendly(value: Date): Object = if (value == null) null else new java.sql.Time(value.getTime)

  private def st(in: Box[Date]): Unit =
  in match {
    case Full(d) => data.set(d); orgData.set(d)
    case _ => data.set(null); orgData.set(null)
  }

  def buildSetActualValue(accessor: Method, v: AnyRef, columnName: String): (T, AnyRef) => Unit =
  (inst, v) => doField(inst, accessor, {case f: MappedTime[T] => f.st(toDate(v))})

  def buildSetLongValue(accessor: Method, columnName: String): (T, Long, Boolean) => Unit =
  (inst, v, isNull) => doField(inst, accessor, {case f: MappedTime[T] => f.st(if (isNull) Empty else Full(new Date(v)))})

  def buildSetStringValue(accessor: Method, columnName: String): (T, String) => Unit =
  (inst, v) => doField(inst, accessor, {case f: MappedTime[T] => f.st(toDate(v))})

  def buildSetDateValue(accessor: Method, columnName: String): (T, Date) => Unit =
  (inst, v) => doField(inst, accessor, {case f: MappedTime[T] => f.st(Full(v))})

  def buildSetBooleanValue(accessor: Method, columnName: String): (T, Boolean, Boolean) => Unit =
  (inst, v, isNull) => doField(inst, accessor, {case f: MappedTime[T] => f.st(Empty)})

  /**
   * Given the driver type, return the string required to create the column in the database
   */
  def fieldCreatorString(dbType: DriverType, colName: String): String = colName + " " + dbType.timeColumnType + notNullAppender()
  
  
  override def toString = if(get==null) "NULL" else format(get)
}

