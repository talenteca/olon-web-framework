package olon

import common.{Empty, Full}
import scala.xml.NodeSeq


/**
 * The util package object
 */
package object util {
  type CssBindFunc = CssSel

  /**
   * Wrap a function and make sure it's a NodeSeq => NodeSeq.  Much easier
   * than explicitly casting the first parameter
   *
   * @param f the function
   * @return a NodeSeq => NodeSeq
   */
  def nsFunc(f: NodeSeq => NodeSeq): NodeSeq => NodeSeq = f

  /**
   * Promote to an IterableConst when implicits won't do it for you
   *
   * @param ic the thing that can be promoted to an IterableConst
   * @param f the implicit function that takes T and makes it an IterableConst
   * @tparam T the type of the parameter
   * @return an IterableConst
   */
  def itConst[T](ic: T)(implicit f: T => IterableConst): IterableConst = f(ic)
}
