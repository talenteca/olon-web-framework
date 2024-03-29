package olon
package util

import scala.collection.mutable.ListBuffer

/** Calculate the edit distance between words
  */
object LD {
  private def min(a: Int, b: Int): Int = if (a < b) a else b
  private def min(a: Int, b: Int, c: Int): Int = min(min(a, b), c)

  /** Find the word that has the closest edit distance to the root string
    *
    * @param root
    *   the root word to compare the other words to
    * @param ly
    *   the list of words to test against Root
    *
    * @return
    *   a Tuple containing the word with the shortest edit distance and the edit
    *   distance
    */
  def apply(root: String, ly: List[String]): (String, Int) =
    this(root, ly, (a: String) => a)

  /** Find the word that has the closest edit distance to the root string
    *
    * @param root
    *   the root word to compare the other words to
    * @param ly
    *   the list of items to test against Root
    * @param f
    *   the function that converts the item to a String
    *
    * @return
    *   a Tuple containing item with the shortest edit distance and the edit
    *   distance
    */
  def apply[T](root: String, ly: List[T], f: T => String): (T, Int) =
    (ly: @unchecked) match {
      case w :: Nil => (w, this(root, f(w)))

      case w :: ws =>
        val tv = this(root, f(w))
        val rest = this(root, ws, f)
        if (tv < rest._2) (w, tv)
        else rest
    }

  /** calculate the edit or <a
    * href='http://en.wikipedia.org/wiki/Levenshtein_distance'>Levenshtein
    * distance</a> between two words
    *
    * @param x
    *   the first word to compare
    * @param y
    *   the second word to compare
    *
    * @return
    *   the edit distance between the words
    */
  def apply(x: String, y: String): Int = {
    val x1 = x.trim.toLowerCase.toList
    val y1 = y.trim.toLowerCase.toList

    def column(
        word: List[Char],
        dist: List[Int],
        left: Int,
        top: Int,
        ch: Char,
        acc: ListBuffer[Int]
    ): List[Int] =
      word match {
        case Nil => acc.toList
        case c :: cs =>
          val cost = if (c == ch) 0 else 1
          val i = dist.head
          val calc = min(left + cost, i + 1, top + 1)
          acc += calc
          column(cs, dist.tail, i, calc, ch, acc)
      }

    def matrix(word: List[Char], pos: Int, dist: List[Int]): List[Int] =
      word match {
        case Nil => dist
        case c :: cs =>
          matrix(cs, pos + 1, column(x1, dist, pos, pos + 1, c, new ListBuffer))
      }

    matrix(y1, 0, (1 to x.length).toList) match {
      case Nil => 100000
      case xs  => xs.last
    }
  }
}
