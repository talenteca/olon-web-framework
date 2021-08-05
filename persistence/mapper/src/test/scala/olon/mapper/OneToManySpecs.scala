package olon {
package mapper {

import org.specs2.mutable.Specification

class OneToManySpecs extends Specification  {
  "One to Many Specification".title
  sequential

  val provider = DbProviders.H2MemoryProvider

  private def ignoreLogger(f: => AnyRef): Unit = ()
  def setupDB: Unit = {
    MapperRules.createForeignKeys_? = c => false
    provider.setupDB
    Schemifier.destroyTables_!!(ignoreLogger _,  Contact, Phone)
    Schemifier.schemify(true, ignoreLogger _, Contact, Phone)
  }

  "OneToMany" should {
    "detect all MappedOneToMany fields" in {
      setupDB
      val contact = Contact.create
      val fields = contact.oneToManyFields
      fields.length must_== 1
      fields(0).asInstanceOf[Any] must_== contact.phones
    }
    "cascade delete" in {
      val contact = Contact.create
      contact.phones += Phone.create
      contact.save

      Contact.count must_== 1
      Phone.count must_== 1

      contact.delete_!

      Contact.count must_== 0
      Phone.count must_== 0
    }
  }

}



class Contact extends LongKeyedMapper[Contact] with IdPK with OneToMany[Long, Contact] {
  def getSingleton = Contact
  object phones extends MappedOneToMany(Phone, Phone.contact) with Cascade[Phone]
}
object Contact extends Contact with LongKeyedMetaMapper[Contact]

class Phone extends LongKeyedMapper[Phone] with IdPK {
  def getSingleton = Phone
  object contact extends MappedLongForeignKey(this, Contact)
  object number extends MappedString(this, 10)
}
object Phone extends Phone with LongKeyedMetaMapper[Phone]


}
}
