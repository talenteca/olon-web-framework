package olon
package util

import javax.mail.internet.{MimeMessage, MimeMultipart}

import org.specs2.mutable.Specification

import common._

import Mailer.{From, To, Subject, PlainMailBodyType, XHTMLMailBodyType, XHTMLPlusImages, PlusImageHolder}

import scala.io.Source

trait MailerForTesting {
  def lastMessage_=(message: Box[MimeMessage]): Unit
  def lastMessage: Box[MimeMessage]
}

/**
 * Systems under specification for Lift Mailer.
 */
class MailerSpec extends Specification {
  "Mailer Specification".title
  sequential

  Props.mode // touch the lazy val so it's detected correctly

  val myMailer = new Mailer with MailerForTesting {
    @volatile var lastMessage: Box[MimeMessage] = Empty

    testModeSend.default.set((msg: MimeMessage) => {
      lastMessage = Full(msg)
    })
  }

  import myMailer._

  private def doNewMessage(send: => Unit): MimeMessage = {
    lastMessage = Empty

    send

    eventually {
      lastMessage.isEmpty must_== false
    }
    lastMessage openOrThrowException("Checked")
  }

  "A Mailer" should {

    "deliver simple messages as simple messages" in {
      val msg = doNewMessage {
        sendMail(
          From("sender@nowhere.com"),
          Subject("This is a simple email"),
          To("recipient@nowhere.com"),
          PlainMailBodyType("Here is some plain text.")
        )
      }

      msg.getContent must beAnInstanceOf[String]
    }

    "deliver multipart messages as multipart" in {
      val msg = doNewMessage {
        sendMail(
          From("sender@nowhere.com"),
          Subject("This is a multipart email"),
          To("recipient@nowhere.com"),
          PlainMailBodyType("Here is some plain text."),
          PlainMailBodyType("Here is some more plain text.")
        )
      }

      msg.getContent must beAnInstanceOf[MimeMultipart]
    }

    "deliver rich messages as multipart" in {
      val msg = doNewMessage {
        sendMail(
          From("sender@nowhere.com"),
          Subject("This is a rich email"),
          To("recipient@nowhere.com"),
          XHTMLMailBodyType(<html> <body>Here is some rich text</body> </html>)
        )
      }

      msg.getContent must beAnInstanceOf[MimeMultipart]
    }

    "deliver emails with attachments as mixed multipart" in {
      val attachmentBytes = Source.fromInputStream(
        getClass.getClassLoader.getResourceAsStream("olon/util/Html5ParserSpec.page1.html")
      ).map(_.toByte).toArray
      val msg = doNewMessage {
        sendMail(
          From("sender@nowhere.com"),
          Subject("This is a mixed email"),
          To("recipient@nowhere.com"),
          XHTMLPlusImages(
            <html> <body>Here is some rich text</body> </html>,
            PlusImageHolder("awesome.pdf", "text/html", attachmentBytes, true)
          )
        )
      }

      msg.getContent must beLike {
        case mp: MimeMultipart =>
          mp.getContentType.substring(0, 21) must_== "multipart/alternative"

          mp.getBodyPart(0).getContent must beLike {
            case mp2: MimeMultipart =>
              mp2.getContentType.substring(0, 15) must_== "multipart/mixed"
          }
      }
    }
  }
}
