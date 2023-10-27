package olon 
package util 

import common._

/**
 * LRU Cache wrapping `org.apache.commons.collections.map.LRUMap`
 *
 * @param size the maximum number of Elements allowed in the LRU map
 * @param loadFactor the Load Factor to construct our LRU with.
 */
class LRU[KeyType, ValueType](size: Int, loadFactor: Box[Float]) extends olon.common.LRUMap[KeyType, ValueType](size, loadFactor) {
  // Alternate constructor that gives you no load factor.
  def this(size: Int) = this(size, Empty)
}

