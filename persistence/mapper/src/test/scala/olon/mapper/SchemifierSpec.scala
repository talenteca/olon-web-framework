package olon
package mapper

import org.specs2.mutable.Specification


/**
 * Systems under specification for Schemifier.
 */
class SchemifierSpec extends Specification  {
  "Schemifier Specification".title

  val provider = DbProviders.H2MemoryProvider
  
  "Schemifier" should {
    "not crash in readonly if table doesn't exist" in {
      provider.setupDB
      Schemifier.schemify(false, Schemifier.neverF _, Thing)
      success
    }
  }
}

