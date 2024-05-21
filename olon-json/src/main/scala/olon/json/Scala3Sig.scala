package olon.json
import scala.quoted._
import scala.tasty.inspector._

object Scala3SigReader {

  def readConstructor(
      argName: String,
      clazz: Class[?],
      typeArgIndex: Int,
      argNames: List[String]
  ): Class[?] = {
    given staging.Compiler = staging.Compiler.make(this.getClass.getClassLoader)

    staging.withQuotes {
      import quotes.reflect._
      // TypeRepr.typeConstructorOf(clazz) // <- did not work
      val cl = Symbol.classSymbol(clazz.getCanonicalName())

      val argNamesWithSymbols = // TODO think about an alternative
        argNames.map(_.replace("$minus", "-"))
      val cstr = findConstructor(cl, argNamesWithSymbols).getOrElse(
        Meta.fail("Can't find constructor for " + clazz)
      )
      findArgType(cstr, argNames.indexOf(argName), typeArgIndex)
    }
  }

  private def findConstructor(using
      quotes: Quotes
  )(
      cl: quotes.reflect.Symbol,
      argNames: List[String]
  ): Option[quotes.reflect.Symbol] = { // TODO may not work with empty argNames
    import quotes.reflect._
    (cl.methodMembers :+ cl.primaryConstructor)
      .filter(_.isClassConstructor)
      .filter { m =>
        if (m.paramSymss(0).headOption.map(_.isTerm).getOrElse(false)) { // def method(arg...)
          m.paramSymss(0).map(_.name).sameElements(argNames)
        } else if (m.paramSymss(1).headOption.map(_.isTerm).getOrElse(false)) { // def method[T...](arg...)
          m.paramSymss(1).map(_.name).sameElements(argNames)
        } else false
      }
      .headOption
  }

  private def findArgType(using quotes: Quotes)(
      s: quotes.reflect.Symbol,
      argIdx: Int,
      typeArgIndex: Int
  ): Class[?] = {
    // println("findArgType " + typeArgIndex)
    import quotes.reflect._
    def findPrimitive(t: TypeRepr): Symbol =
      // println("findPrimitive " + t)
      def throwError() = Meta.fail("Unexpected type info " + t.show)
      // println(t.typeSymbol)
      if defn.ScalaPrimitiveValueClasses.contains(t.typeSymbol) then
        t.typeSymbol
      else if t.typeArgs.isEmpty then
        t.typeSymbol // TODO investigate when this rhs should not be accessed
      else
        t match
          case AppliedType(_, typeArgs) if typeArgs.size <= typeArgIndex =>
            findPrimitive(typeArgs(0))
          case AppliedType(_, typeArgs) =>
            findPrimitive(typeArgs(typeArgIndex))
          case _ => throwError()

    toClass(
      findPrimitive(
        if (s.paramSymss(0).headOption.map(_.isTerm).getOrElse(false)) { // def method(arg...)
          s.typeRef.memberType(s.paramSymss(0)(argIdx))
        } else if (s.paramSymss(1).headOption.map(_.isTerm).getOrElse(false)) { // def method[T...](arg...)
          s.typeRef.memberType(s.paramSymss(1)(argIdx))
        } else Meta.fail("Incorrect function signature " + s)
      )
    )
  }

  def readField(name: String, clazz: Class[?], typeArgIndex: Int): Class[?] = {
    given staging.Compiler = staging.Compiler.make(this.getClass.getClassLoader)

    println("readField " + name + " " + clazz + " " + typeArgIndex)
    staging.withQuotes { // (quotes: Quotes) ?=>
      import quotes.reflect._
      val sym = Symbol.classSymbol(
        clazz.getCanonicalName()
      ) // TypeRepr.typeConstructorOf(clazz).typeSymbol
      val methodSymbolMaybe =
        sym.fieldMembers
          .filter(_.name == name)
          .headOption // TODO add iteration through parents
      findArgTypeForField(methodSymbolMaybe.get, typeArgIndex)
    }

    // IDEA: use tasty-inspector-like library to achieve the same effect, while executing less phases
    // like in tasty-inspector. The one provided with scala does not allow us to use ByteStreams
    // (via read resource as stream) or get the classes via parent classloader
    // Below is an unsuccessful attempt to use unchanged tasty-inspector

    // val name = clazz.getName
    // val subPath = name.substring(name.lastIndexOf('.') + 1) + ".tasty"

    // abstract class InspectorWithResult extends Inspector {
    //   var result: Option[Class[?]] = None
    // }
    // val inspector = new InspectorWithResult {
    //   // override var result: Option[Class[?]] = None
    //   def inspect(using Quotes)(tastys: List[Tasty[quotes.type]]): Unit = {
    //     import quotes.reflect._
    //     val sym = Symbol.classSymbol(
    //       clazz.getCanonicalName()
    //     ) // TypeRepr.typeConstructorOf(clazz).typeSymbol
    //     val methodSymbolMaybe =
    //       sym.fieldMembers
    //         .filter(_.name == name)
    //         .headOption // TODO add iteration through parents
    //     result = Some(findArgTypeForField(methodSymbolMaybe.get, typeArgIndex))
    //   }
    // }
    // TastyInspector.inspectTastyFiles(List(subPath))(inspector)
    // println("result: " + inspector.result.get)
    // inspector.result.get
  }

  private def findArgTypeForField(using
      quotes: Quotes
  )(methodSymbol: quotes.reflect.Symbol, typeArgIdx: Int) = {
    import quotes.reflect._
    val t =
      methodSymbol.owner.typeRef.memberType(methodSymbol).widen.dealias match
        case MethodType(paramNames, paramTypes, retTpe) =>
          paramTypes(typeArgIdx)
        case AppliedType(_, args) =>
          // will be entered when methodSymbol is a var
          args(typeArgIdx)

    def findPrimitive(t: TypeRepr): Symbol =
      if defn.ScalaPrimitiveValueClasses.contains(t.typeSymbol) then
        t.typeSymbol
      else Meta.fail("Unexpected type info " + t.show)
    toClass(findPrimitive(t))
  }

  private def toClass(using quotes: Quotes)(
      s: quotes.reflect.Symbol
  ): Class[?] =
    import quotes.reflect._
    s.fullName match {
      case "scala.Short"   => classOf[Short]
      case "scala.Int"     => classOf[Int]
      case "scala.Long"    => classOf[Long]
      case "scala.Boolean" => classOf[Boolean]
      case "scala.Float"   => classOf[Float]
      case "scala.Double"  => classOf[Double]
      case _               => classOf[AnyRef]
    }
}
