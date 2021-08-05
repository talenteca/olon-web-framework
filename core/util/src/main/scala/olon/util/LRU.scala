package olon 
package util 

import common._

/**
 * LRU Cache wrapping {@link org.apache.commons.collections.map.LRUMap}
 *
 * @param size the maximum number of Elements allowed in the LRU map
 * @param loadFactor the Load Factor to construct our LRU with.
 */
class LRU[KeyType, ValueType](size: Int, loadFactor: Box[Float]) extends olon.common.LRUMap[KeyType, ValueType](size, loadFactor) {
  // Alternate constructor that gives you no load factor.
  def this(size: Int) = this(size, Empty)

  /*

  private val map = loadFactor match {
    case Full(lf) => new LRUMap(size, lf)
    case _ => new LRUMap(size)
  }

  def update(k: KeyType, v: ValueType) {
    map.put(k, v)
  }

  def remove(k: KeyType) = map.remove(k)

  def get(k: KeyType): Box[ValueType] =
  if (map.containsKey(k)) Full(this.apply(k))
  else Empty

  def apply(k: KeyType): ValueType = map.get(k).asInstanceOf[ValueType]
  def contains(k: KeyType): Boolean = map.containsKey(k)
  def keys: List[KeyType] = map.keySet().toList.map(_.asInstanceOf[KeyType])
  */
}

