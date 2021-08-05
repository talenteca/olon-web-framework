package olon
package mongodb
package record

import org.specs2.mutable.Specification

import org.specs2.concurrent.ExecutionEnv

class MongoRecordAsyncSpec(implicit ee: ExecutionEnv) extends Specification with MongoAsyncTestKit {
  "MongoRecord Async Specification".title

  import fixtures.FieldTypeTestRecord

  "MongoRecord Async" should {

    "insert asynchronously" in {
      checkMongoIsRunning

      val obj = FieldTypeTestRecord.createRecord
        .mandatoryLongField(42L)
        .mandatoryIntField(27)

      FieldTypeTestRecord.insertAsync(obj) must beEqualTo[Boolean](true).await

      val fetched = FieldTypeTestRecord.find(obj.id.get)

      fetched.isDefined must_== true

      fetched.foreach { o =>
        o.id.get must_== obj.id.get
        o.mandatoryLongField.get must_== 42L
        o.mandatoryIntField.get must_== 27
      }

      success
    }

    "replaceOne asynchronously" in {
      checkMongoIsRunning

      val obj = FieldTypeTestRecord.createRecord
        .mandatoryLongField(42L)
        .mandatoryIntField(27)

      FieldTypeTestRecord.replaceOneAsync(obj) must beEqualTo[FieldTypeTestRecord](obj).await

      val fetched = FieldTypeTestRecord.find(obj.id.get)

      fetched.isDefined must_== true

      fetched.foreach { o =>
        o.id.get must_== obj.id.get
        o.mandatoryLongField.get must_== 42L
        o.mandatoryIntField.get must_== 27
      }

      obj
        .mandatoryLongField(44L)
        .mandatoryIntField(29)

      FieldTypeTestRecord.replaceOneAsync(obj) must beEqualTo[FieldTypeTestRecord](obj).await

      val fetched2 = FieldTypeTestRecord.find(obj.id.get)

      fetched2.isDefined must_== true

      fetched2.foreach { o =>
        o.id.get must_== obj.id.get
        o.mandatoryLongField.get must_== 44L
        o.mandatoryIntField.get must_== 29
      }

      success
    }

    "replaceOne without upsert" in {
      checkMongoIsRunning

      val obj = FieldTypeTestRecord.createRecord

      FieldTypeTestRecord.replaceOneAsync(obj, false) must beEqualTo[FieldTypeTestRecord](obj).await
      FieldTypeTestRecord.find(obj.id.get).isDefined must_== false
    }
  }
}
