package olon
package mapper

import scala.xml.Node
import olon.http.SHtml

/**
 * This trait can be added to existing Mapper fields to make them use AjaxUtils.editable
 * for field display.
 */
trait AjaxEditableField[FieldType,OwnerType <: Mapper[OwnerType]] extends MappedField[FieldType,OwnerType] {
  override def asHtml : Node =
    if (editableField) {
      <xml:group>{
        toForm.map { form =>
          SHtml.ajaxEditable(super.asHtml, form, () => {fieldOwner.save; onSave(); olon.http.js.JsCmds.Noop})
        } openOr super.asHtml
      }</xml:group>
    } else {
      super.asHtml
    }

  /** This method is called when the element's data are saved. The default is to do nothing */
  def onSave(): Unit = {}

  /** This method allows you to do programmatic control of whether the field will display
   *  as editable. The default is true */
  def editableField = true
}

