package olon
package mongodb
package record

import org.specs2.mutable.Specification

class BsonRecordSpec extends Specification with MongoTestKit {
  "BsonRecordSpec Specification".title

  import fixtures._
  import testmodels._

  override def before = {
    super.before
    checkMongoIsRunning
  }

  "BsonRecord" should {
    "compare properly with set values" in {

      val subRec = SubSubRecord.createRecord.name("subrecord")
      val subRec2 = SubSubRecord.createRecord.name("subrecord")

      (subRec == subRec2) must_== true

      subRec2.name("subrecord2")

      (subRec == subRec2) must_== false

    }

    "compare properly with default values" in {
      val subRec = SubSubRecord.createRecord
      val subRec2 = SubSubRecord.createRecord

      (subRec == subRec2) must_== true
    }
  }
}
