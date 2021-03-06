package olon
package mapper

import java.sql.Types
import java.lang.reflect.Method
import java.util.Date
import olon.util._
import Helpers._
import olon.common._
import olon.http.js._
import olon.json._
import scala.reflect.runtime.universe._
import scala.xml.{Text, NodeSeq}
import json.JsonAST.JValue


abstract class MappedBinary[T<:Mapper[T]](val fieldOwner: T) extends MappedField[Array[Byte], T] {
  private val data : FatLazy[Array[Byte]] =  FatLazy(defaultValue)
  private val orgData: FatLazy[Array[Byte]] = FatLazy(defaultValue)

  protected def real_i_set_!(value : Array[Byte]) : Array[Byte] = {
    data() = value
    this.dirty_?( true)
    value
  }

  def manifest: TypeTag[Array[Byte]] = typeTag[Array[Byte]]

  /**
   * Get the source field metadata for the field
   * @return the source field metadata for the field
   */
  def sourceInfoMetadata(): SourceFieldMetadata{type ST = Array[Byte]} =
    SourceFieldMetadataRep(name, manifest, new FieldConverter {
    /**
     * The type of the field
     */
    type T = Array[Byte]

    /**
     * Convert the field to a String
     * @param v the field value
     * @return the string representation of the field value
     */
    def asString(v: T): String = ""

    /**
     * Convert the field into NodeSeq, if possible
     * @param v the field value
     * @return a NodeSeq if the field can be represented as one
     */
    def asNodeSeq(v: T): Box[NodeSeq] = Empty

    /**
     * Convert the field into a JSON value
     * @param v the field value
     * @return the JSON representation of the field
     */
    def asJson(v: T): Box[JValue] = Empty

    /**
     * If the field can represent a sequence of SourceFields,
     * get that
     * @param v the field value
     * @return the field as a sequence of SourceFields
     */
    def asSeq(v: T): Box[Seq[SourceFieldInfo]] = Empty
  })

  def dbFieldClass: Class[Array[Byte]] = classOf[Array[Byte]]

  /**
  * Get the JDBC SQL Type for this field
  */
  //  def getTargetSQLType(field : String) = Types.BINARY
  def targetSQLType: Int = Types.BINARY

  def defaultValue: Array[Byte] = null
  override def writePermission_? = true
  override def readPermission_? = true

  protected def i_is_! : Array[Byte] = data.get

  protected def i_was_! : Array[Byte] = orgData.get

  protected[mapper] def doneWithSave(): Unit = {orgData.setFrom(data)}

  protected def i_obscure_!(in : Array[Byte]) : Array[Byte] = {
    new Array[Byte](0)
  }

  override def renderJs_? = false

  def asJsExp: JsExp = throw new NullPointerException("No way")

  def asJsonValue: Box[JsonAST.JValue] = Full(get match {
    case null => JsonAST.JNull
    case value => JsonAST.JString(base64Encode(value))
  })

  override def setFromAny(f: Any): Array[Byte] = f match {
    case null | JsonAST.JNull => this.set(null)
    case JsonAST.JString(base64) => this.set(base64Decode(base64))
    case array: Array[Byte] => this.set(array)
    case s => this.set(s.toString.getBytes("UTF-8"))
  }

  def jdbcFriendly(field : String) : Object = get

  def real_convertToJDBCFriendly(value: Array[Byte]): Object = value

  def buildSetActualValue(accessor: Method, inst: AnyRef, columnName: String): (T, AnyRef) => Unit =
  (inst, v) => doField(inst, accessor, {case f: MappedBinary[T] =>
    val toSet = v match {
      case null => null
      case ba: Array[Byte] => ba
      case other => other.toString.getBytes("UTF-8")
    }
    f.data() = toSet
    f.orgData() = toSet
  })

  def buildSetLongValue(accessor : Method, columnName : String): (T, Long, Boolean) => Unit = null
  def buildSetStringValue(accessor : Method, columnName : String): (T, String) => Unit  = null
  def buildSetDateValue(accessor : Method, columnName : String): (T, Date) => Unit = null
  def buildSetBooleanValue(accessor : Method, columnName : String): (T, Boolean, Boolean) => Unit = null

  /**
  * Given the driver type, return the string required to create the column in the database
  */
  def fieldCreatorString(dbType: DriverType, colName: String): String = colName + " " + dbType.binaryColumnType + notNullAppender()
}

abstract class MappedText[T<:Mapper[T]](val fieldOwner: T) extends MappedField[String, T] {
  private val data : FatLazy[String] =  FatLazy(defaultValue)
  private val orgData: FatLazy[String] = FatLazy(defaultValue)

  protected def real_i_set_!(value: String): String = {
    data() = value
    this.dirty_?( true)
    value
  }


  def manifest: TypeTag[String] = typeTag[String]

  /**
   * Get the source field metadata for the field
   * @return the source field metadata for the field
   */
  def sourceInfoMetadata(): SourceFieldMetadata{type ST = String} =
    SourceFieldMetadataRep(name, manifest, new FieldConverter {
      /**
       * The type of the field
       */
      type T = String

      /**
       * Convert the field to a String
       * @param v the field value
       * @return the string representation of the field value
       */
      def asString(v: T): String = v

      /**
       * Convert the field into NodeSeq, if possible
       * @param v the field value
       * @return a NodeSeq if the field can be represented as one
       */
      def asNodeSeq(v: T): Box[NodeSeq] = Full(Text(v))

      /**
       * Convert the field into a JSON value
       * @param v the field value
       * @return the JSON representation of the field
       */
      def asJson(v: T): Box[JValue] = Full(JString(v))

      /**
       * If the field can represent a sequence of SourceFields,
       * get that
       * @param v the field value
       * @return the field as a sequence of SourceFields
       */
      def asSeq(v: T): Box[Seq[SourceFieldInfo]] = Empty
    })

  def dbFieldClass: Class[String] = classOf[String]

  /**
  * Get the JDBC SQL Type for this field
  */
  //  def getTargetSQLType(field : String) = Types.BINARY
  def targetSQLType: Int = Types.VARCHAR

  def defaultValue: String = null
  override def writePermission_? = true
  override def readPermission_? = true

  protected def i_is_! : String = data.get

  protected def i_was_! : String = orgData.get

  protected[mapper] def doneWithSave(): Unit = {orgData.setFrom(data)}

  def asJsExp: JsExp = JE.Str(get)

  def asJsonValue: Box[JsonAST.JValue] = Full(get match {
    case null => JsonAST.JNull
    case str => JsonAST.JString(str)
  })

  protected def i_obscure_!(in: String): String = ""

  override def setFromAny(in: Any): String = {
    in match {
      case JsonAST.JNull => this.set(null)
      case JsonAST.JString(str) => this.set(str)
      case seq: Seq[_] if seq.nonEmpty => seq.map(setFromAny).head
      case (s: String) :: _ => this.set(s)
      case s :: _ => this.setFromAny(s)
      case null => this.set(null)
      case s: String => this.set(s)
      case Some(s: String) => this.set(s)
      case Full(s: String) => this.set(s)
      case None | Empty | Failure(_, _, _) => this.set(null)
      case o => this.set(o.toString)
    }
  }

  def jdbcFriendly(field : String): Object = real_convertToJDBCFriendly(data.get)

  def real_convertToJDBCFriendly(value: String): Object = value match {
    case null => null
    case s => s
  }

  def buildSetActualValue(accessor: Method, inst: AnyRef, columnName: String): (T, AnyRef) => Unit =
  (inst, v) => doField(inst, accessor, {case f: MappedText[T] =>
    val toSet = v match {
      case null => null
      case s: String => s
      case ba: Array[Byte] => new String(ba, "UTF-8")
      case clob: java.sql.Clob => clob.getSubString(1,clob.length.toInt)
      case other => other.toString
    }
    f.data() = toSet
    f.orgData() = toSet
  })

  def buildSetLongValue(accessor : Method, columnName : String): (T, Long, Boolean) => Unit = null
  def buildSetStringValue(accessor : Method, columnName : String): (T, String) => Unit  = (inst, v) => doField(inst, accessor, {case f: MappedText[T] =>
    val toSet = v
    f.data() = toSet
    f.orgData() = toSet
  })
  def buildSetDateValue(accessor : Method, columnName : String): (T, Date) => Unit = null
  def buildSetBooleanValue(accessor : Method, columnName : String): (T, Boolean, Boolean) => Unit = null

  /**
  * Given the driver type, return the string required to create the column in the database
  */
  def fieldCreatorString(dbType: DriverType, colName: String): String = colName + " " + dbType.clobColumnType + notNullAppender()
}

abstract class MappedFakeClob[T<:Mapper[T]](val fieldOwner: T) extends MappedField[String, T] {
  private val data : FatLazy[String] =  FatLazy(defaultValue)
  private val orgData: FatLazy[String] = FatLazy(defaultValue)

  protected def real_i_set_!(value: String): String = {
    data() = value
    this.dirty_?( true)
    value
  }

  def dbFieldClass: Class[String] = classOf[String]

  def manifest: TypeTag[String] = typeTag[String]

  /**
   * Get the source field metadata for the field
   * @return the source field metadata for the field
   */
  def sourceInfoMetadata(): SourceFieldMetadata{type ST = String} =
    SourceFieldMetadataRep(name, manifest, new FieldConverter {
      /**
       * The type of the field
       */
      type T = String

      /**
       * Convert the field to a String
       * @param v the field value
       * @return the string representation of the field value
       */
      def asString(v: T): String = v

      /**
       * Convert the field into NodeSeq, if possible
       * @param v the field value
       * @return a NodeSeq if the field can be represented as one
       */
      def asNodeSeq(v: T): Box[NodeSeq] = Full(Text(v))

      /**
       * Convert the field into a JSON value
       * @param v the field value
       * @return the JSON representation of the field
       */
      def asJson(v: T): Box[JValue] = Full(JString(v))

      /**
       * If the field can represent a sequence of SourceFields,
       * get that
       * @param v the field value
       * @return the field as a sequence of SourceFields
       */
      def asSeq(v: T): Box[Seq[SourceFieldInfo]] = Empty
    })


  /**
  * Get the JDBC SQL Type for this field
  */
  //  def getTargetSQLType(field : String) = Types.BINARY
  def targetSQLType: Int = Types.BINARY

  def defaultValue: String = null
  override def writePermission_? = true
  override def readPermission_? = true

  protected def i_is_! : String = data.get

  protected def i_was_! : String = orgData.get

  protected[mapper] def doneWithSave(): Unit = {orgData.setFrom(data)}

  protected def i_obscure_!(in: String): String = ""

  def asJsExp: JsExp = JE.Str(get)

  def asJsonValue: Box[JsonAST.JValue] = Full(get match {
    case null => JsonAST.JNull
    case str => JsonAST.JString(str)
  })

  override def setFromAny(in: Any): String = {
    in match {
      case JsonAST.JNull => this.set(null)
      case JsonAST.JString(str) => this.set(str)
      case seq: Seq[_] if seq.nonEmpty => seq.map(setFromAny).head
      case (s: String) :: _ => this.set(s)
      case s :: _ => this.setFromAny(s)
      case null => this.set(null)
      case s: String => this.set(s)
      case Some(s: String) => this.set(s)
      case Full(s: String) => this.set(s)
      case None | Empty | Failure(_, _, _) => this.set(null)
      case o => this.set(o.toString)
    }
  }

  def jdbcFriendly(field : String): Object = real_convertToJDBCFriendly(data.get)

  def real_convertToJDBCFriendly(value: String): Object = value match {
    case null => null
    case s => s.getBytes("UTF-8")
  }

  def buildSetActualValue(accessor: Method, inst: AnyRef, columnName: String): (T, AnyRef) => Unit =
  (inst, v) => doField(inst, accessor, {case f: MappedFakeClob[T] =>
    val toSet = v match {
      case null => null
      case ba: Array[Byte] => new String(ba, "UTF-8")
      case clob: java.sql.Clob => clob.getSubString(1,clob.length.toInt)
      case other => other.toString
    }
    f.data() = toSet
    f.orgData() = toSet
  })

  def buildSetLongValue(accessor : Method, columnName : String): (T, Long, Boolean) => Unit = null
  def buildSetStringValue(accessor : Method, columnName : String): (T, String) => Unit = (inst, v) => doField(inst, accessor, {case f: MappedFakeClob[T] =>
    val toSet = v
    f.data() = toSet
    f.orgData() = toSet
  })
  def buildSetDateValue(accessor : Method, columnName : String): (T, Date) => Unit = null
  def buildSetBooleanValue(accessor : Method, columnName : String): (T, Boolean, Boolean) => Unit = null

  /**
  * Given the driver type, return the string required to create the column in the database
  */
  def fieldCreatorString(dbType: DriverType, colName: String): String = colName + " " + dbType.binaryColumnType + notNullAppender()
}
