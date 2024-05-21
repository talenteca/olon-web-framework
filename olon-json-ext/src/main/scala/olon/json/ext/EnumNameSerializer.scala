package olon
package json
package ext

// SCALA3 Adding `using val` for `_enum` to access `E#Value` as `_enum.Value`
// SCALA3 FIXME `json.Serializer[E#Value]` and `json.Serializer[_enum.Value]` are
// failing, using `json.Serializer[Any]` instead which will force an external
// explicit casting when using this serializer
class EnumNameSerializer[E <: Enumeration](implicit val _enum: E)
    extends json.Serializer[Any] {
  import JsonDSL._

  val EnumerationClass = classOf[_enum.Value]

  def deserialize(implicit
      format: Formats
  ): PartialFunction[(TypeInfo, JValue), _enum.Value] = {
    case (TypeInfo(EnumerationClass, _), json) =>
      json match {
        case JString(value) if (_enum.values.exists(_.toString == value)) =>
          _enum.withName(value)
        case value =>
          throw new MappingException(
            "Can't convert " +
              value + " to " + EnumerationClass
          )
      }
  }

  def serialize(implicit format: Formats): PartialFunction[Any, JValue] = {
    case i: _enum.Value => i.toString
  }
}
