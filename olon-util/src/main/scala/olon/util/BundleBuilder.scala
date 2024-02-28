package olon
package util

import olon.common._

import java.util.Enumeration
import java.util.Locale
import java.util.ResourceBundle
import scala.xml._

/** Converts a NodeSeq of a particular format into a ResourceBundle. Basically,
  * each of the second-level nodes that contain the attribute "name" the name
  * becomes an entry in the resulting resource bundle. It is possible to
  * localize each of the entries with the lang and country attributes which will
  * be compared against the incoming Locale. If the default attribute is true,
  * then that entry is used if no others match. Note that language is weighted
  * more heavily than country.<br/><br/> If the node is a Text or PCData node,
  * it will be returned as a String. Otherwise, it will be returned as a
  * NodeSeq.
  */
object BundleBuilder {
  private object IsText {
    def unapply(in: NodeSeq): Option[String] = in.toList match {
      case (x: Atom[_]) :: Nil => Some(x.text)
      case _                   => None
    }
  }

  final private case class EntryInfo(
      name: String,
      lang: Option[String],
      country: Option[String],
      default: Boolean
  )

  /** Convers
    */
  def convert(nodes: NodeSeq, loc: Locale): Box[ResourceBundle] = {
    val country = Some(loc.getCountry()).filter(_.length > 0)
    val lang = Some(loc.getLanguage()).filter(_.length > 0)

    val vals: List[ResourceBundle] =
      nodes.toList.flatMap {
        case e: Elem => {
          val all: List[(EntryInfo, NodeSeq)] =
            e.child.toList.flatMap {
              case e: Elem => {
                e.attribute("name")
                  .toList
                  .map(attr =>
                    EntryInfo(
                      attr.text,
                      e.attribute("lang").map(_.text),
                      e.attribute("country").map(_.text),
                      e.attribute("default")
                        .map(_.text)
                        .flatMap(Helpers.asBoolean) getOrElse false
                    ) -> (e.child: NodeSeq)
                  )
              }

              case _ => Nil
            }

          val map =
            all.foldLeft[Map[String, List[(EntryInfo, NodeSeq)]]](Map()) {
              case (map, pair @ (info, _)) =>
                map + (info.name -> (pair :: map.getOrElse(info.name, Nil)))
            }

          def points(i: EntryInfo): Int = {
            (if (i.lang == lang) 4 else 0) +
              (if (i.country == country) 2 else 0) +
              (if (i.default) 1 else 0)
          }

          def choose(lst: List[(EntryInfo, NodeSeq)]): NodeSeq =
            lst.reduceLeft { (a, b) =>
              {
                val ap = points(a._1)
                val bp = points(b._1)
                if (ap > bp) {
                  a
                } else if (bp > ap) {
                  b
                } else if (a._1.default) a
                else b
              }
            }._2

          // SCALA3 using `x*` instead of `x: _*`
          val res: Map[String, NodeSeq] = Map(map.map { case (name, lst) =>
            name -> choose(lst)
          }.toSeq*)

          List(new ResourceBundle {
            def getKeys(): Enumeration[String] = {
              val it = res.keys.iterator
              new Enumeration[String] {
                def hasMoreElements() = it.hasNext
                def nextElement() = it.next()
              }
            }

            def handleGetObject(key: String): Object =
              res.get(key) match {
                case Some(IsText(str)) => str
                case Some(ns)          => ns
                case _                 => null
              }
          })
        }

        case _ => Nil
      }

    vals.headOption
  }
}
