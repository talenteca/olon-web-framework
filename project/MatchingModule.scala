import sbt._
import Keys._

/**
 * Pattern-matches an attributed file, extracting its module organization,
 * name, and revision if available in its attributes.
 */
object MatchingModule {
  def unapply(file: Attributed[File]): Option[(String,String,String)] = {
    file.get(moduleID.key).map { moduleInfo =>
      (moduleInfo.organization, moduleInfo.name, moduleInfo.revision)
    }
  }
}
