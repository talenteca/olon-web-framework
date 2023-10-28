package olon
package json
package ext

import org.specs2.mutable.Specification

import common._
import json.Serialization.{read, write => swrite}

/** System under specification for JsonBoxSerializer.
  */
class JsonBoxSerializerSpec extends Specification {
  "JsonBoxSerializer Specification".title

  implicit val formats = olon.json.DefaultFormats + new JsonBoxSerializer

  "Extract empty age" in {
    parse("""{"name":"joe"}""")
      .extract[Person] mustEqual Person("joe", Empty, Empty)
  }

  "Extract boxed thing" in {
    parse("""{"name":"joe", "thing": "rog", "age":12}""")
      .extract[Person] mustEqual Person("joe", Full(12), Empty, Full("rog"))
  }

  "Extract boxed mother" in {
    val json =
      """{"name":"joe", "age":12, "mother": {"name":"ann", "age":53}}"""
    val p = parse(json).extract[Person]
    p mustEqual Person("joe", Full(12), Full(Person("ann", Full(53), Empty)))
    (for {
      a1 <- p.age; m <- p.mother; a2 <- m.age
    } yield a1 + a2) mustEqual Full(65)
  }

  "Render with age" in {
    swrite(
      Person("joe", Full(12), Empty)
    ) mustEqual """{"name":"joe","age":12,"mother":null,"thing":null}"""
  }

  "Serialize failure" in {
    val exn1 = SomeException("e1")
    val exn2 = SomeException("e2")
    val p = Person(
      "joe",
      Full(12),
      Failure("f", Full(exn1), Failure("f2", Full(exn2), Empty))
    )
    val ser = swrite(p)
    read[Person](ser) mustEqual p
  }

  "Serialize param failure" in {
    val exn = SomeException("e1")
    val p = Person(
      "joe",
      Full(12),
      ParamFailure("f", Full(exn), Empty, "param value")
    )
    val ser = swrite(p)
    read[Person](ser) mustEqual p
  }
}

case class SomeException(msg: String) extends Exception

case class Person(
    name: String,
    age: Box[Int],
    mother: Box[Person],
    thing: Box[String] = Empty
)
