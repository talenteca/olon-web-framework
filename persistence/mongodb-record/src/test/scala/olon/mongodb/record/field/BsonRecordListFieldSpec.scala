package olon
package mongodb
package record
package field

import org.specs2.mutable.Specification
import olon.common._
import olon.record.field.StringField

package bsonlistfieldspecs {
  class BookShelf extends MongoRecord[BookShelf] with ObjectIdPk[BookShelf] {
    def meta = BookShelf

    object books extends BsonRecordListField(this, Book)
  }
  object BookShelf extends BookShelf with MongoMetaRecord[BookShelf] {
    override def collectionName = "bookshelf"
  }

  class Book extends BsonRecord[Book] {
    override def meta = Book

    object title extends StringField(this, 512)
  }
  object Book extends Book with BsonMetaRecord[Book]
}

class BsonRecordListFieldSpec extends Specification {
  "BsonRecordListField Specification".title

  import bsonlistfieldspecs._

  "BsonRecordListFieldSpec" should {

    "fail validation if at least one of its elements fails validation" in {
      val scalaBook = Book.createRecord.title("Programming in Scala")
      val liftBook = Book.createRecord
      liftBook.title.setBox(Failure("Bad format"))
      val shelf = BookShelf.createRecord.books(scalaBook :: liftBook :: Nil)

      shelf.validate must have size(1)
    }

    "pass validation if all of its elements pass validation" in {
      val scalaBook = Book.createRecord.title("Programming in Scala")
      val liftBook = Book.createRecord.title("Simply Lift")
      val shelf = BookShelf.createRecord.books(scalaBook :: liftBook :: Nil)

      shelf.validate must be empty
    }

  }
}
