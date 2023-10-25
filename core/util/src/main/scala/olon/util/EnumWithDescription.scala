package olon 
package util 



/*
 A wrapper arround a Scala Enumeration Value that has a name, description for each object
 */
trait ValueWithDescription  {
    def description: String
    def name: String
}

abstract class EnumWithDescription  {
  import scala.language.reflectiveCalls

    private var _values: List[_enum.Value with ValueWithDescription] = Nil
    def values = _values

    // possibly not a good idea using this directly
    val _enum = new Enumeration {
        def Value(inName: String, inDescription: String): Value with ValueWithDescription = {
            new Val(nextId, inName) with ValueWithDescription {
                def description = inDescription
                def name = inName
            }
        }
    }

    def Value(name: String, description: String): _enum.Value with ValueWithDescription = {
        val value = _enum.Value(name, description)
        _values = _values ::: List(value)  // build in order
        value
    }

    def Value(name: String): _enum.Value with ValueWithDescription = Value(name, name)

    def valueOf(name: String) = values find (_.name == name)

    def nameDescriptionList = values map(x => (x.name, x.description) )

}

