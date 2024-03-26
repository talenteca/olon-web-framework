package olon.json
import scala.quoted.staging

object Scala3Sig {
  given staging.Compiler = staging.Compiler.make(getClass.getClassLoader)

  def readConstructor(
      argName: String,
      clazz: Class[?],
      typeArgIndex: Int,
      argNames: List[String]
  ): Class[?] = ???

  def readField(name: String, clazz: Class[?], typeArgIndex: Int): Class[?] =
    ???
}
