package olon
package util

import java.lang.ref.ReferenceQueue
import java.lang.ref.SoftReference
import java.util._

import Map._
import concurrent.locks._
import common._
import util._
import Helpers._

/** Companion module that has the role of monitoring garbage collected
  * references and remove the orphaned keys from the cache. The monitor is
  * started by calling <i>initialize</i> function and terminated by calling
  * <i>shutDown</i>. It monitors all SoftReferenceCache instances in the context
  * of the same classloader. It can also be used as a factory for obtaining new
  * instances of SoftReferenceCache class
  */
object SoftReferenceCache {

  @volatile
  private var terminated = false;

  private[util] val refQueue = new ReferenceQueue[Any]();

  /** Create a new SoftReferenceCache instance
    */
  def apply[K, V](size: Int) = new SoftReferenceCache[K, V](size)

  /** Initialize the orphan keys monitor
    */
  def initialize = {
    // A daemon thread is more approapriate here then an Actor as
    // we'll do blocking reads from the reference queue
    val thread = new Thread(new Runnable() {
      def run(): Unit = {
        processQueue
      }
    })
    thread.setDaemon(true)
    thread.setContextClassLoader(null)
    thread.start
  }

  /** ShutDown the monitoring
    */
  def shutDown = {
    terminated = true;
  }

  private def processQueue: Unit = {
    while (!terminated) {
      tryo {
        // Wait 30 seconds for something to appear in the queue.
        // SCALA3 using `?` instead of `_`
        val sftVal = refQueue.remove(30000).asInstanceOf[SoftValue[?, ?]];
        if (sftVal != null) {
          sftVal.cache.remove(sftVal.key);
        }
      }
    }
  }
}

case object ProcessQueue

case object Done

/** A Map that holds the values as SoftReference-s. It also applies a LRU policy
  * for the cache entries.
  */
class SoftReferenceCache[K, V](cacheSize: Int) {

  val cache = new LinkedHashMap[K, SoftValue[K, V]]() {
    override def removeEldestEntry(
        eldest: Entry[K, SoftValue[K, V]]
    ): Boolean = {
      return size() > cacheSize;
    }
  }

  val rwl = new ReentrantReadWriteLock();

  val readLock = rwl.readLock
  val writeLock = rwl.writeLock

  private def lock[T](l: Lock)(block: => T): T = {
    l.lock

    try {
      block
    } finally {
      l.unlock
    }
  }

  /** Returns the cached value mapped with this key or Empty if not found
    *
    * @param key
    * @return
    *   Box[V]
    */
  def apply(key: K): Box[V] = {
    val result: (Boolean, Box[V]) /* (doRemove, retval) */ =
      lock(readLock) {
        Box.!!(cache.get(key)) match {
          case Full(value) =>
            Box.!!(value.get).map(value => (false, Full(value))) openOr {
              (true, Empty)
            }
          case _ => (false, Empty)
        }
      }

    result match {
      case (doRemove, retval) if doRemove =>
        lock(writeLock) {
          val value = cache.get(key)

          if (value != null && value.get == null)
            remove(key)
        }

        retval
      case (_, retval) =>
        retval
    }
  }

  /** Puts a new keyed entry in cache
    * @param tuple:
    *   (K, V)*
    * @return
    *   this
    */
  def +=(tuple: (K, V)*) = {
    lock(writeLock) {
      for (t <- tuple) yield {
        cache.put(
          t._1,
          new SoftValue(t._1, t._2, this, SoftReferenceCache.refQueue)
        );
      }
    }
    this
  }

  /** Removes the cache entry mapped with this key
    *
    * @return
    *   the value removed
    */
  def remove(key: Any): Box[V] = {
    lock(writeLock) {
      for {
        value <- Box.!!(cache.remove(key).asInstanceOf[SoftValue[K, V]])
        realValue <- Box.!!(value.get)
      } yield realValue
    }
  }

  def keys = cache.keySet

}

class SoftValue[K, V](
    k: K,
    v: V,
    lruCache: SoftReferenceCache[K, V],
    queue: ReferenceQueue[Any]
) extends SoftReference[V](v, queue) {
  def key: K = k

  def cache: SoftReferenceCache[K, V] = lruCache
}
