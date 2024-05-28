package olon
package json
package ext

// SCALA3 Adding `using val` for `_enum` to access `E#Value` as `_enum.Value`
// SCALA3 FIXME `json.Serializer[E#Value]` and `json.Serializer[_enum.Value]` are
// failing, using `json.Serializer[Any]` instead which will force an external
// explicit casting when using this serializer
class EnumSerializer[E <: Enumeration, V <: Enumeration#Value](val _enum: E)(
    implicit ev: _enum.Value =:= V
) extends json.Serializer[V] {
  import JsonDSL._

  val EnumerationClass = classOf[_enum.Value]

  def deserialize(implicit
      format: Formats
  ): PartialFunction[(TypeInfo, JValue), V] = {
    case (TypeInfo(EnumerationClass, _), json) =>
      json match {
        case JInt(value) if (value <= _enum.maxId) => ev(_enum(value.toInt))
        case value =>
          throw new MappingException(
            "Can't convert " +
              value + " to " + EnumerationClass
          )
      }
  }

  def serialize(implicit format: Formats): PartialFunction[Any, JValue] = {
    case i: _enum.Value => i.id
  }
}