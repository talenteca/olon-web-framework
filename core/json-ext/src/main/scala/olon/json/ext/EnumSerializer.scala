package olon
package json
package ext

import scala.reflect.ClassTag

class EnumSerializer[E <: Enumeration: ClassTag](enum: E)
  extends json.Serializer[E#Value] {
  import JsonDSL._

  val EnumerationClass = classOf[E#Value]

  def deserialize(implicit format: Formats):
    PartialFunction[(TypeInfo, JValue), E#Value] = {
      case (TypeInfo(EnumerationClass, _), json) => json match {
        case JInt(value) if (value <= enum.maxId) => enum(value.toInt)
        case value => throw new MappingException("Can't convert " +
          value + " to "+ EnumerationClass)
      }
    }

  def serialize(implicit format: Formats): PartialFunction[Any, JValue] = {
    case i: E#Value => i.id
  }
}

class EnumNameSerializer[E <: Enumeration: ClassTag](enum: E)
  extends json.Serializer[E#Value] {
  import JsonDSL._

  val EnumerationClass = classOf[E#Value]

  def deserialize(implicit format: Formats):
    PartialFunction[(TypeInfo, JValue), E#Value] = {
      case (TypeInfo(EnumerationClass, _), json) => json match {
        case JString(value) if (enum.values.exists(_.toString == value)) =>
          enum.withName(value)
        case value => throw new MappingException("Can't convert " +
          value + " to "+ EnumerationClass)
      }
    }

  def serialize(implicit format: Formats): PartialFunction[Any, JValue] = {
    case i: E#Value => i.toString
  }
}
