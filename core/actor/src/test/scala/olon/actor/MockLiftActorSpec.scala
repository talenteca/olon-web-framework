package olon
package actor

import org.specs2.mutable.Specification

class MockLiftActorSpec extends Specification {
  "Mock Actor Specification".title

  sealed trait MockSpecActorMessage
  case object MockSpecActorMessage1 extends MockSpecActorMessage
  case object MockSpecActorMessage2 extends MockSpecActorMessage
  case object MockSpecActorMessage3 extends MockSpecActorMessage

  "A MockSpecializedLiftActor" should {
    "correctly indicate when it has received a message" in {
      val mockActor = new MockSpecializedLiftActor[MockSpecActorMessage]

      mockActor ! MockSpecActorMessage1
      mockActor ! MockSpecActorMessage2

      mockActor.hasReceivedMessage_?(MockSpecActorMessage1) must beTrue
    }

    "correctly indicate when it has not received a message" in {
      val mockActor = new MockSpecializedLiftActor[MockSpecActorMessage]

      mockActor ! MockSpecActorMessage1
      mockActor ! MockSpecActorMessage2

      mockActor.hasReceivedMessage_?(MockSpecActorMessage3) must beFalse
    }

    "correctly indicate the number of messages it has received" in {
      val mockActor = new MockSpecializedLiftActor[MockSpecActorMessage]

      mockActor ! MockSpecActorMessage1
      mockActor ! MockSpecActorMessage2
      mockActor ! MockSpecActorMessage3

      mockActor.messageCount must_== 3
    }

    "correctly list the messages it has received" in {
      val mockActor = new MockSpecializedLiftActor[MockSpecActorMessage]

      mockActor ! MockSpecActorMessage1
      mockActor ! MockSpecActorMessage2
      mockActor ! MockSpecActorMessage3

      mockActor.messages must_== List(
        MockSpecActorMessage3,
        MockSpecActorMessage2,
        MockSpecActorMessage1
      )
    }
  }

  "A MockLiftActor" should {
    "correctly indicate when it has received a message" in {
      val mockActor = new MockLiftActor

      mockActor ! MockSpecActorMessage1
      mockActor ! MockSpecActorMessage2

      mockActor.hasReceivedMessage_?(MockSpecActorMessage1) must beTrue
    }

    "correctly indicate when it has not received a message" in {
      val mockActor = new MockLiftActor

      mockActor ! MockSpecActorMessage1
      mockActor ! MockSpecActorMessage2

      mockActor.hasReceivedMessage_?(MockSpecActorMessage3) must beFalse
    }

    "correctly indicate the number of messages it has received" in {
      val mockActor = new MockLiftActor

      mockActor ! MockSpecActorMessage1
      mockActor ! MockSpecActorMessage2
      mockActor ! MockSpecActorMessage3

      mockActor.messageCount must_== 3
    }

    "correctly list the messages it has received" in {
      val mockActor = new MockLiftActor

      mockActor ! MockSpecActorMessage1
      mockActor ! MockSpecActorMessage2
      mockActor ! MockSpecActorMessage3

      mockActor.messages must_== List(
        MockSpecActorMessage3,
        MockSpecActorMessage2,
        MockSpecActorMessage1
      )
    }
  }
}
