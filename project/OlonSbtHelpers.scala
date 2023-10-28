import sbt._
import Keys._

object OlonSbtHelpers {
  def coreProject = olonProject("core") _
  def webProject = olonProject("web") _

  /** Project definition helper that simplifies creation of `ProjectReference`.
    *
    * It is a convenience method to create a Olon `ProjectReference` module by
    * having the boilerplate for most common activities tucked in.
    *
    * @param base
    *   the base path location of project module.
    * @param prefix
    *   the prefix of project module.
    * @param module
    *   the name of the project module. Typically, a project id is of the form
    *   olon-`module`.
    */
  def olonProject(base: String, prefix: String = "olon-")(
      module: String
  ): Project =
    olonProject(
      id = if (module.startsWith(prefix)) module else prefix + module,
      base = file(base) / module.stripPrefix(prefix)
    )

  def olonProject(id: String, base: File): Project = {
    Project(id, base)
      .settings(
        scalacOptions ++= List(
          "-feature",
          "-language:implicitConversions",
          "-deprecation"
        )
      )
      .settings(
        autoAPIMappings := true,
        apiMappings ++= {
          val cp: Seq[Attributed[File]] = (Compile / fullClasspath).value
          val apiUrlSeq = cp map { entry =>
            entry.get(moduleID.key) match {
              case Some(moduleInfo) =>
                findApiUrl(moduleInfo, scalaBinaryVersion.value) match {
                  case Some(apiUrl) =>
                    Some((entry.data -> apiUrl))
                  case None =>
                    None
                }
              case None =>
                None
            }
          }
          apiUrlSeq.filter(_.nonEmpty).map(_.get).toMap
        }
      )
  }

  private def findApiUrl(
      moduleInfo: ModuleID,
      scalaBinaryVersionString: String
  ): Option[URL] = {
    if (moduleInfo.organization == "com.talenteca") {
      None
    } else {
      Some(
        url(
          "https://www.javadoc.io/doc/" + moduleInfo.organization + "/" + moduleInfo.name + "/" + moduleInfo.revision + "/"
        )
      )
    }
  }
}
