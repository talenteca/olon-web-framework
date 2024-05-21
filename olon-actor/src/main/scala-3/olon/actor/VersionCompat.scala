package olon.actor

import scala.compiletime.uninitialized

// TODO add modifiers
private[actor] object VersionCompat:
  trait LAFutureCompat[T] {
    // SCALA3 using `uninitialized` instead of `_`
    private[actor] var item: T = uninitialized
  }
  trait LASchedulerCompat {
    // SCALA3 using `uninitialized` instead of `_`
    @volatile
    private[actor] var exec: ILAExecute = uninitialized
  }
  trait MailboxItemCompat[T] {
    // SCALA3 using `uninitialized` instead of `_`
    private[actor] var next: MailboxItem[T] = uninitialized
    private[actor] var prev: MailboxItem[T] = uninitialized
  }
