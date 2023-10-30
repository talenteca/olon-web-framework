package olon
package http
package js
package extcore

import org.specs2.mutable.Specification

/** System under specification for ExtCoreArtifacts.
  */
class ExtCoreArtifactsSpec extends Specification {
  "ExtCoreArtifacts Specification".title

  "ExtCoreArtifacts.toggle" should {
    "return the correct javascript expression" in {
      ExtCoreArtifacts.toggle("id").toJsCmd must_== """Ext.fly("id").toggle()"""
    }
  }

  "ExtCoreArtifacts.hide" should {
    "return the correct javascript expression" in {
      ExtCoreArtifacts.hide("id").toJsCmd must_== """Ext.fly("id").hide()"""
    }
  }

  "ExtCoreArtifacts.show" should {
    "return the correct javascript expression" in {
      ExtCoreArtifacts.show("id").toJsCmd must_== """Ext.fly("id").show()"""
    }
  }

  "ExtCoreArtifacts.showAndFocus" should {
    "return the correct javascript expression" in {
      ExtCoreArtifacts
        .showAndFocus("id")
        .toJsCmd must_== """Ext.fly("id").show().focus(200)"""
    }
  }

  "ExtCoreArtifacts.serialize" should {
    "return the correct javascript expression" in {
      ExtCoreArtifacts
        .serialize("id")
        .toJsCmd must_== """Ext.Ajax.serializeForm("id")"""
    }
  }
}
