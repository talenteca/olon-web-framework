package olon.actor

private[actor] object VersionCompat {
  trait LAFutureCompat[T] {
    private[actor] var item: T = _
  }
  trait LASchedulerCompat {
    @volatile
    private[actor] var exec: ILAExecute = _
  }
  trait MailboxItemCompat[T] {
    private[actor] var next: MailboxItem[T] = _
    private[actor] var prev: MailboxItem[T] = _
  }
}
