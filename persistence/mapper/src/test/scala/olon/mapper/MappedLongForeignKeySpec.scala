package olon
package mapper

import org.specs2.mutable.Specification

import common._


/**
 * Systems under specification for MappedLongForeignKey.
 */
class MappedLongForeignKeySpec extends Specification with org.specs2.specification.BeforeEach {
  "MappedLongForeignKey Specification".title
  sequential

  // Make sure we have everything configured first
  MapperSpecsModel.setup()

  def provider = DbProviders.H2MemoryProvider

  def before: Unit = MapperSpecsModel.cleanup()

  "MappedLongForeignKey" should {
      (try {
        provider.setupDB
      } catch {
        case e if !provider.required_? => 1 must be_==(2).orSkip("Provider %s not available: %s".format(provider, e))
      }) must not(throwA[Exception]).orSkip

    "Not allow comparison to another FK" in {
      val dog = Dog.create.name("Froo").saveMe
      val user = {
        def ret: User = {
          val r = User.create.saveMe
          if (r.id.get >= dog.id.get) r
          else ret
        }

        ret
      }
      dog.owner(user).save
      val d2 = Dog.find(dog.id).openOrThrowException("Test")
      d2.id.get must_== user.id.get
      (d2.owner == user) must_== true
      (d2.owner == d2) must_== false
    }

    "be primed after setting a reference" in {
      val dog = Dog.create
      val user = User.create
      dog.owner(user)
      dog.owner.obj.isDefined must beTrue
    }
    
    "be primed after setting a Boxed reference" in {
      val dog = Dog.create
      val user = User.create
      dog.owner(Full(user))
      dog.owner.obj.isDefined must beTrue
    }
    
    "be empty after setting an Empty" in {
      val user = User.create
      val dog = Dog.create.owner(user)
      dog.owner(Empty)
      
      dog.owner.obj must_== Empty
      dog.owner.get must_== 0L
    }
  }
}

