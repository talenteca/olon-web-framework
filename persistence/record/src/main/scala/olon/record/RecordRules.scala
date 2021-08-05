package olon
package record

import http.Factory
import util.ConnectionIdentifier
import util.Helpers._

import java.util.Locale

object RecordRules extends Factory {
  /**
    * Calculate the name of a field based on the name
    * of the Field. Must be set in Boot before any code
    * that touches the MetaRecord.
    *
    * To get snake_case, use this:
    *
    *  RecordRules.fieldName.default.set((_, name) => StringHelpers.snakify(name))
    */
  val fieldName = new FactoryMaker[(ConnectionIdentifier, String) => String]((_: ConnectionIdentifier, name: String) => name) {}

  /**
    * This function is used to calculate the displayName of a field. Can be
    * used to easily localize fields based on the locale in the
    * current request
    */
  val displayName: FactoryMaker[(Record[_], Locale, String) => String] =
    new FactoryMaker[(Record[_], Locale, String) => String]((m: Record[_], l: Locale, name: String) => name) {}
}
