package olon
package util

import org.specs2.matcher.XmlMatchers
import org.specs2.mutable.Specification

import java.util.Locale
import scala.xml.NodeSeq

/** Systems under specification for BundleBuilder.
  */
class BundleBuilderSpec extends Specification with XmlMatchers {
  "BundleBuilder Specification".title

  "BundleBuilder" should {
    "Build a Bundle" in {
      val b = BundleBuilder
        .convert(
          <div>
                                    <div name="dog" lang="en">Dog</div>
                                    <div name="dog" lang="fr">Chien</div>
                                    <div name="cat"><div>hi</div></div>
                                    </div>,
          Locale.US
        )
        .openOrThrowException("Test")

      b.getObject("dog") must_== "Dog"
      b.getObject("cat").asInstanceOf[NodeSeq] must ==/(<div>hi</div>)
    }

    "Build a Bundle must support default" in {
      val b = BundleBuilder
        .convert(
          <div>
                                    <div name="dog" lang="zz">Dog</div>
                                    <div name="dog" lang="fr" default="true" >Chien</div>
                                    <div name="cat"><div>hi</div></div>
                                    </div>,
          Locale.US
        )
        .openOrThrowException("Test")

      b.getObject("dog") must_== "Chien"
      b.getObject("cat").asInstanceOf[NodeSeq] must ==/(<div>hi</div>)
    }

  }
}
