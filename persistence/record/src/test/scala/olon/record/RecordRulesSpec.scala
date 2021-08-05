package olon
package record

import common._
import http.{LiftSession, S}
import util.Helpers._

import org.specs2.mutable._

import fixtures._


/**
 * Systems under specification for RecordField.
 */
class RecordRulesSpec extends Specification {
  "Record Rules Specification".title
  sequential

  "RecordRules" should {
    "snakify custom field name" in {
      RecordRules.fieldName.doWith((_, name) => snakify(name)) {
        val rec = BasicTestRecord.createRecord

        rec.fieldThree.name must_== "field_three"
      }
    }
    "camelify custom field display name" in {
      RecordRules.displayName.doWith((_, _, name) => camelify(name)) {
        val rec = BasicTestRecord.createRecord

        rec.fieldThree.displayName must_== "FieldThree"
      }
    }
  }
}
