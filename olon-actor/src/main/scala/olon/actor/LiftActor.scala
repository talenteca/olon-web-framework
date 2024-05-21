package olon
package actor


import common._

trait ILAExecute {
  def execute(f: () => Unit): Unit
  def shutdown(): Unit
}

/** The definition of a scheduler
  */
trait LAScheduler {

  /** Execute some code on another thread
    *
    * @param f
    *   the function to execute on another thread
    */
  def execute(f: () => Unit): Unit
}

object LAScheduler
    extends LAScheduler
    with Loggable
    with VersionCompat.LASchedulerCompat {
  @volatile
  var onSameThread = false

  /** Set this variable to the number of threads to allocate in the thread pool
    */
  @volatile var threadPoolSize = 16 // issue 194

  @volatile var maxThreadPoolSize = threadPoolSize * 25

  /** If it's Full, then create an ArrayBlockingQueue, otherwise create a
    * LinkedBlockingQueue. Default to Full(200000).
    */
  @volatile var blockingQueueSize: Box[Int] = Full(200000)

  @volatile
  var createExecutor: () => ILAExecute = () => {
    new ILAExecute {
      import java.util.concurrent._

      private val es = // Executors.newFixedThreadPool(threadPoolSize)
        new ThreadPoolExecutor(
          threadPoolSize,
          maxThreadPoolSize,
          60,
          TimeUnit.SECONDS,
          blockingQueueSize match {
            case Full(x) =>
              new ArrayBlockingQueue(x)
            case _ => new LinkedBlockingQueue
          }
        )

      def execute(f: () => Unit): Unit =
        es.execute(new Runnable {
          def run(): Unit = {
            try {
              f()
            } catch {
              case e: Exception => logger.error("Lift Actor Scheduler", e)
            }
          }
        })

      def shutdown(): Unit = {
        es.shutdown()
      }
    }
  }

  /** Execute some code on another thread
    *
    * @param f
    *   the function to execute on another thread
    */
  def execute(f: () => Unit): Unit = {
    synchronized {
      if (exec eq null) {
        exec = createExecutor()
      }
      exec.execute(f)
    }
  }

  def shutdown(): Unit = {
    synchronized {
      if (exec ne null) {
        exec.shutdown()
      }

      exec = null
    }
  }
}

private[actor] class MailboxItem[T](val item: T)
    extends VersionCompat.MailboxItemCompat[T] {

  /*
  def find(f: MailboxItem => Boolean): Box[MailboxItem] =
  if (f(this)) Full(this) else next.find(f)
   */

  def remove(): Unit = {
    prev.next = next
    next.prev = prev
  }

  def insertAfter(newItem: MailboxItem[T]): MailboxItem[T] = {
    next.prev = newItem
    newItem.prev = this
    newItem.next = this.next
    next = newItem
    newItem
  }

  def insertBefore(newItem: MailboxItem[T]): MailboxItem[T] = {
    prev.next = newItem
    newItem.prev = this.prev
    newItem.next = this
    prev = newItem
    newItem
  }
}

trait SpecializedLiftActor[T] extends SimpleActor[T] {
  @volatile private var processing = false
  private val baseMailbox: MailboxItem[T] = new SpecialMailbox
  @volatile private var msgList: List[T] = Nil
  @volatile private var priorityMsgList: List[T] = Nil
  @volatile private var startCnt = 0

  private class SpecialMailbox extends MailboxItem(null.asInstanceOf[T]) {
    // override def find(f: MailboxItem => Boolean): Box[MailboxItem] = Empty
    next = this
    prev = this
  }

  private def findMailboxItem(
      start: MailboxItem[T],
      f: MailboxItem[T] => Boolean
  ): Box[MailboxItem[T]] =
    start match {
      case _: SpecialMailbox => Empty
      case x if f(x)         => Full(x)
      case x                 => findMailboxItem(x.next, f)
    }

  /** Send a message to the Actor. This call will always succeed and return
    * almost immediately. The message will be processed asynchronously. This is
    * a Java-callable alias for !.
    */
  def send(msg: T): Unit = this ! msg

  /** Send a message to the Actor. This call will always succeed and return
    * almost immediately. The message will be processed asynchronously.
    */
  def !(msg: T): Unit = {
    val toDo: () => Unit = baseMailbox.synchronized {
      msgList ::= msg
      if (!processing) {
        if (LAScheduler.onSameThread) {
          processing = true
          () => processMailbox(true)
        } else {
          if (startCnt == 0) {
            startCnt += 1
            () => LAScheduler.execute(() => processMailbox(false))
          } else
            () => {}
        }
      } else () => {}
    }
    toDo()
  }

  /** This method inserts the message at the head of the mailbox. It's protected
    * because this functionality may or may not want to be exposed.
    */
  protected def insertMsgAtHeadOfQueue_!(msg: T): Unit = {
    val toDo: () => Unit = baseMailbox.synchronized {
      this.priorityMsgList ::= msg
      if (!processing) {
        if (LAScheduler.onSameThread) {
          processing = true
          () => processMailbox(true)
        } else {
          if (startCnt == 0) {
            startCnt += 1
            () => LAScheduler.execute(() => processMailbox(false))
          } else
            () => {}
        }
      } else () => {}
    }
    toDo()
  }

  private def processMailbox(ignoreProcessing: Boolean): Unit = {
    around {
      proc2(ignoreProcessing)
    }
  }

  /** A list of LoanWrappers that will be executed around the evaluation of
    * mailboxes
    */
  protected def aroundLoans: List[CommonLoanWrapper] = Nil

  /** You can wrap calls around the evaluation of the mailbox. This allows you
    * to set up the environment.
    */
  protected def around[R](f: => R): R = aroundLoans match {
    case Nil => f
    case xs  => CommonLoanWrapper(xs)(f)
  }
  private def proc2(ignoreProcessing: Boolean): Unit = {
    var clearProcessing = true
    baseMailbox.synchronized {
      if (!ignoreProcessing && processing) return
      processing = true
      if (startCnt > 0) startCnt = 0
    }

    val eh = exceptionHandler

    def putListIntoMB(): Unit = {
      if (!priorityMsgList.isEmpty) {
        priorityMsgList.foldRight(baseMailbox)((msg, mb) =>
          mb.insertAfter(new MailboxItem(msg))
        )
        priorityMsgList = Nil
      }

      if (!msgList.isEmpty) {
        msgList.foldLeft(baseMailbox)((mb, msg) =>
          mb.insertBefore(new MailboxItem(msg))
        )
        msgList = Nil
      }
    }

    try {
      while (true) {
        baseMailbox.synchronized {
          putListIntoMB()
        }

        var keepOnDoingHighPriory = true

        while (keepOnDoingHighPriory) {
          val hiPriPfBox = highPriorityReceive
          hiPriPfBox
            .map { hiPriPf =>
              findMailboxItem(
                baseMailbox.next,
                mb => testTranslate(hiPriPf.isDefinedAt)(mb.item)
              ) match {
                case Full(mb) =>
                  mb.remove()
                  try {
                    execTranslate(hiPriPf)(mb.item)
                  } catch {
                    case e: Exception => if (eh.isDefinedAt(e)) eh(e)
                  }
                case _ =>
                  baseMailbox.synchronized {
                    if (msgList.isEmpty) {
                      keepOnDoingHighPriory = false
                    } else {
                      putListIntoMB()
                    }
                  }
              }
            }
            .openOr { keepOnDoingHighPriory = false }
        }

        val pf = messageHandler

        findMailboxItem(
          baseMailbox.next,
          mb => testTranslate(pf.isDefinedAt)(mb.item)
        ) match {
          case Full(mb) =>
            mb.remove()
            try {
              execTranslate(pf)(mb.item)
            } catch {
              case e: Exception => if (eh.isDefinedAt(e)) eh(e)
            }
          case _ =>
            baseMailbox.synchronized {
              if (msgList.isEmpty) {
                processing = false
                clearProcessing = false
                return
              } else {
                putListIntoMB()
              }
            }
        }
      }
    } catch {
      case exception: Throwable =>
        if (eh.isDefinedAt(exception))
          eh(exception)

        throw exception
    } finally {
      if (clearProcessing) {
        baseMailbox.synchronized {
          processing = false
        }
      }
    }
  }

  protected def testTranslate(f: T => Boolean)(v: T): Boolean = f(v)

  protected def execTranslate(f: T => Unit)(v: T): Unit = f(v)

  protected def messageHandler: PartialFunction[T, Unit]

  protected def highPriorityReceive: Box[PartialFunction[T, Unit]] = Empty

  protected def exceptionHandler: PartialFunction[Throwable, Unit] = { case e =>
    ActorLogger.error("Actor threw an exception", e)
  }
}

/** A SpecializedLiftActor designed for use in unit testing of other components.
  *
  * Messages sent to an actor extending this interface are not processed, but
  * are instead recorded in a List. The intent is that when you are testing some
  * other component (say, a snippet) that should send a message to an actor, the
  * test for that snippet should simply test that the actor received the
  * message, not what the actor does with that message. If an actor implementing
  * this trait is injected into the component you're testing (in place of the
  * real actor) you gain the ability to run these kinds of tests.
  */
class MockSpecializedLiftActor[T] extends SpecializedLiftActor[T] {
  private var messagesReceived: List[T] = Nil

  /** Send a message to the mock actor, which will be recorded and not processed
    * by the message handler.
    */
  override def !(msg: T): Unit = {
    messagesReceived.synchronized {
      messagesReceived ::= msg
    }
  }

  // We aren't required to implement a real message handler for the Mock actor
  // since the message handler never runs.
  override def messageHandler: PartialFunction[T, Unit] = { case _ =>
  }

  /** Test to see if this actor has received a particular message.
    */
  def hasReceivedMessage_?(msg: T): Boolean = messagesReceived.contains(msg)

  /** Returns the list of messages the mock actor has received.
    */
  def messages: List[T] = messagesReceived

  /** Return the number of messages this mock actor has received.
    */
  def messageCount: Int = messagesReceived.size
}

object ActorLogger extends Logger {}

private final case class MsgWithResp(msg: Any, future: LAFuture[Any])

trait LiftActor
    extends SpecializedLiftActor[Any]
    with GenericActor[Any]
    with ForwardableActor[Any, Any] {
  @volatile
  private var responseFuture: LAFuture[Any] = null

  protected final def forwardMessageTo(
      msg: Any,
      forwardTo: TypedActor[Any, Any]
  ): Unit = {
    if (null ne responseFuture) {
      forwardTo match {
        case la: LiftActor => la ! MsgWithResp(msg, responseFuture)
        case other =>
          reply(other !? msg)
      }
    } else forwardTo ! msg
  }

  /** Send a message to the Actor and get an LAFuture that will contain the
    * reply (if any) from the message. This method calls !&lt; and is here for
    * Java compatibility.
    */
  def sendAndGetFuture(msg: Any): LAFuture[Any] = this !< msg

  /** Send a message to the Actor and get an LAFuture that will contain the
    * reply (if any) from the message
    */
  def !<(msg: Any): LAFuture[Any] = {
    val future = new LAFuture[Any]
    this ! MsgWithResp(msg, future)
    future
  }

  /** Send a message to the Actor and wait for the actor to process the message
    * and reply. This method is the Java callable version of !?.
    */
  def sendAndGetReply(msg: Any): Any = this !? msg

  /** Send a message to the Actor and wait for the actor to process the message
    * and reply.
    */
  def !?(msg: Any): Any = {
    val future = new LAFuture[Any]
    this ! MsgWithResp(msg, future)
    future.get()
  }

  /** Send a message to the Actor and wait for up to timeout milliseconds for
    * the actor to process the message and reply. This method is the Java
    * callable version of !?.
    */
  def sendAndGetReply(timeout: Long, msg: Any): Any = this.!?(timeout, msg)

  /** Send a message to the Actor and wait for up to timeout milliseconds for
    * the actor to process the message and reply.
    */
  def !?(timeout: Long, message: Any): Box[Any] =
    this !! (message, timeout)

  /** Send a message to the Actor and wait for up to timeout milliseconds for
    * the actor to process the message and reply.
    */
  def !!(msg: Any, timeout: Long): Box[Any] = {
    val future = new LAFuture[Any]
    this ! MsgWithResp(msg, future)
    future.get(timeout)
  }

  /** Send a message to the Actor and wait for the actor to process the message
    * and reply.
    */
  def !!(msg: Any): Box[Any] = {
    val future = new LAFuture[Any]
    this ! MsgWithResp(msg, future)
    Full(future.get())
  }

  override protected def testTranslate(f: Any => Boolean)(v: Any) = v match {
    case MsgWithResp(msg, _) => f(msg)
    case v                   => f(v)
  }

  override protected def execTranslate(f: Any => Unit)(v: Any) = v match {
    case MsgWithResp(msg, future) =>
      responseFuture = future
      try {
        f(msg)
      } finally {
        responseFuture = null
      }
    case v => f(v)
  }

  /** The Actor should call this method with a reply to the message
    */
  protected def reply(v: Any): Unit = {
    if (null ne responseFuture) {
      responseFuture.satisfy(v)
    }
  }
}

/** A MockLiftActor for use in testing other compnents that talk to actors.
  *
  * Much like MockSpecializedLiftActor, this class is intended to be injected
  * into other components, such as snippets, during testing. Whereas these
  * components would normally talk to a real actor that would process their
  * message, this mock actor simply records them and exposes methods the unit
  * test can use to investigate what messages have been received by the actor.
  */
class MockLiftActor extends MockSpecializedLiftActor[Any] with LiftActor
