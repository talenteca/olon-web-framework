package olon
package mongodb
package record
package field

import org.bson.types.ObjectId
import org.specs2.mutable.Specification

import olon.common._
import olon.json.ext.EnumNameSerializer
import olon.record.field.{EnumNameField, OptionalEnumNameField}
import olon.util.Helpers._

import com.mongodb._

package enumnamefieldspecs {
  object WeekDay extends Enumeration {
    type WeekDay = Value
    val Mon, Tue, Wed, Thu, Fri, Sat, Sun = Value
  }

  case class JsonObj(dow: WeekDay.WeekDay) extends JsonObject[JsonObj] {
    def meta = JsonObj
  }
  object JsonObj extends JsonObjectMeta[JsonObj]

  class EnumNameRec extends MongoRecord[EnumNameRec] with ObjectIdPk[EnumNameRec] {
    def meta = EnumNameRec

    object dow extends EnumNameField(this, WeekDay)
    object dowOptional extends OptionalEnumNameField(this, WeekDay)
    object jsonobj extends JsonObjectField[EnumNameRec, JsonObj](this, JsonObj) {
      def defaultValue = JsonObj(WeekDay.Mon)
    }

    override def equals(other: Any): Boolean = other match {
      case that: EnumNameRec =>
        this.id.get == that.id.get &&
        this.dow.value == that.dow.value &&
        this.dowOptional.valueBox == that.dowOptional.valueBox &&
        this.jsonobj.value == that.jsonobj.value
      case _ => false
    }
  }
  object EnumNameRec extends EnumNameRec with MongoMetaRecord[EnumNameRec] {
    override def collectionName = "enumnamerecs"
    override def formats = super.formats + new EnumNameSerializer(WeekDay)
  }
}


/**
 * Systems under specification for EnumNameField.
 */
class EnumNameFieldSpec extends Specification with MongoTestKit {
  "EnumNameField Specification".title

  import enumnamefieldspecs._

  "EnumNameField" should {

    "work with default values" in {
      checkMongoIsRunning

      val er = EnumNameRec.createRecord.save()

      val erFromDb = EnumNameRec.find(er.id.get)
      erFromDb must beLike {
        case Full(er2) =>
          er2 mustEqual er
          er2.dow.value mustEqual WeekDay.Mon
          er2.dowOptional.valueBox mustEqual Empty
          er2.jsonobj.value mustEqual JsonObj(WeekDay.Mon)
      }
    }

    "work with set values" in {
      checkMongoIsRunning

      val er = EnumNameRec.createRecord
        .dow(WeekDay.Tue)
        .jsonobj(JsonObj(WeekDay.Sun))
        .save()

      val erFromDb = EnumNameRec.find(er.id.get)
      erFromDb must beLike {
        case Full(er2) =>
          er2 mustEqual er
          er2.dow.value mustEqual WeekDay.Tue
          er2.jsonobj.value mustEqual JsonObj(WeekDay.Sun)
      }
    }

    "work with Empty optional values" in {
      checkMongoIsRunning

      val er = EnumNameRec.createRecord
      er.dowOptional.setBox(Empty)
      er.save()

      val erFromDb = EnumNameRec.find(er.id.get)
      erFromDb must beLike {
        case Full(er2) =>
          er2 mustEqual er
          er2.dowOptional.valueBox mustEqual Empty
      }
    }

    "work with Full optional values" in {
      checkMongoIsRunning

      val er = EnumNameRec.createRecord
      er.dowOptional.setBox(Full(WeekDay.Sat))
      er.save()

      val erFromDb = EnumNameRec.find(er.id.get)
      erFromDb must beLike {
        case Full(er2) =>
          er2 mustEqual er
          er2.dowOptional.valueBox mustEqual Full(WeekDay.Sat)
      }
    }
  }
}
