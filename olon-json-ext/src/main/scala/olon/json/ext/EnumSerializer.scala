// package olon
// package json
// package ext

// class EnumSerializer[E <: Enumeration](val _enum: E)
//     extends json.Serializer[_enum.Value] {
//   import JsonDSL._

//   val EnumerationClass = classOf[E#Value]

//   def deserialize(implicit
//       format: Formats
//   ): PartialFunction[(TypeInfo, JValue), E#Value] = {
//     case (TypeInfo(EnumerationClass, _), json) =>
//       json match {
//         case JInt(value) if (value <= _enum.maxId) => _enum(value.toInt)
//         case value =>
//           throw new MappingException(
//             "Can't convert " +
//               value + " to " + EnumerationClass
//           )
//       }
//   }

//   def serialize(implicit format: Formats): PartialFunction[Any, JValue] = {
//     case i: E#Value => i.id
//   }
// }

// class EnumNameSerializer[E <: Enumeration](_enum: E)
//     extends json.Serializer[E#Value] {
//   import JsonDSL._

//   val EnumerationClass = classOf[E#Value]

//   def deserialize(implicit
//       format: Formats
//   ): PartialFunction[(TypeInfo, JValue), E#Value] = {
//     case (TypeInfo(EnumerationClass, _), json) =>
//       json match {
//         case JString(value) if (_enum.values.exists(_.toString == value)) =>
//           _enum.withName(value)
//         case value =>
//           throw new MappingException(
//             "Can't convert " +
//               value + " to " + EnumerationClass
//           )
//       }
//   }

//   def serialize(implicit format: Formats): PartialFunction[Any, JValue] = {
//     case i: E#Value => i.toString
//   }
// }
