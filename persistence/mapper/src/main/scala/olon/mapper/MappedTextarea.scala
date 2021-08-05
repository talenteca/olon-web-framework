package olon
package mapper

import http.S
import common._

import scala.xml.Elem

abstract class MappedTextarea[T<:Mapper[T]](owner : T, maxLen: Int) extends MappedString[T](owner, maxLen) {
  /**
   * Create an input field for the item
   */
  override def _toForm: Box[Elem] = {
    S.fmapFunc({s: List[String] => this.setFromAny(s)}){funcName =>
    Full(appendFieldId(<textarea name={funcName}
	               rows={textareaRows.toString}
	               cols={textareaCols.toString}>{
	   get match {
	     case null => ""
	     case s => s}}</textarea>))}
  }

  override def toString: String = {
    val v = get
    if (v == null || v.length < 100) super.toString
    else v.substring(0,40)+" ... "+v.substring(v.length - 40)
  }

  def textareaRows  = 8

  def textareaCols = 20

}

