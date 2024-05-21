package olon.common

import scala.compiletime.uninitialized

private[common] trait LinkListElemCompat[T2]:
  private[common] var value2: T2 = uninitialized
