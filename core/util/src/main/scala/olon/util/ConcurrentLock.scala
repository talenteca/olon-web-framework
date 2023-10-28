package olon
package util

import java.util.concurrent.locks._

class ConcurrentLock extends ReentrantReadWriteLock {

  def read[T](f: => T): T = {
    readLock().lock()
    try {
      f
    } finally {
      readLock().unlock()
    }
  }

  def write[T](f: => T): T = {
    writeLock().lock()
    try {
      f
    } finally {
      writeLock().unlock()
    }
  }

  def upgrade[T](f: => T): T = {
    readLock().unlock
    writeLock().lock
    try {
      f
    } finally {
      writeLock().unlock
      readLock().lock
    }
  }
}
