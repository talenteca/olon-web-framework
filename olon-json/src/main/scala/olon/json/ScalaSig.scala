package olon
package json

import scala.tools.scalap.scalax.rules.scalasig._

private[json] object ScalaSigReader {
  // Originally, we used `method.children` and expected all children of a
  // MethodSymbol to be parameters. In Scala 2.13, a change was made that never
  // returns parameters in `children`. To get around this, we look up parameter
  // symbols separately here.
  //
  // This works across Scala versions, so we don't scope it to 2.13
  // specifically. See Scala bug 11747, currently at
  // https://github.com/scala/bug/issues/11747 , for more.
  private def paramSymbolsFor(method: MethodSymbol): Seq[Symbol] = {
    method
      .applyScalaSigRule(ScalaSigParsers.symbols)
      .filter(symbol => symbol.parent == Some(method) && symbol.isParam)
  }

  // SCALA3 using `?` instead of `_`
  def readConstructor(
      argName: String,
      clazz: Class[?],
      typeArgIndex: Int,
      argNames: List[String]
  ): Class[?] = {
    val cl = findClass(clazz)
    val cstr = findConstructor(cl, argNames).getOrElse(
      Meta.fail("Can't find constructor for " + clazz)
    )
    findArgType(cstr, argNames.indexOf(argName), typeArgIndex)
  }

  // SCALA3 using `?` instead of `_`
  def readField(name: String, clazz: Class[?], typeArgIndex: Int): Class[?] = {
    def read(current: Class[?]): MethodSymbol = {
      if (current == null)
        Meta.fail("Can't find field " + name + " from " + clazz)
      else
        findField(findClass(current), name).getOrElse(
          read(current.getSuperclass)
        )
    }
    findArgTypeForField(read(clazz), typeArgIndex)
  }

  // SCALA3 using `?` instead of `_`
  private def findClass(clazz: Class[?]): ClassSymbol = {
    val sig = findScalaSig(clazz).getOrElse(
      Meta.fail("Can't find ScalaSig for " + clazz)
    )
    findClass(sig, clazz).getOrElse(
      Meta.fail("Can't find " + clazz + " from parsed ScalaSig")
    )
  }

  // SCALA3 using `?` instead of `_`
  private def findClass(sig: ScalaSig, clazz: Class[?]): Option[ClassSymbol] = {
    sig.symbols
      .collect { case c: ClassSymbol if !c.isModule => c }
      .find(_.name == clazz.getSimpleName)
      .orElse {
        sig.topLevelClasses
          .find(_.symbolInfo.name == clazz.getSimpleName)
          .orElse {
            sig.topLevelObjects.map { obj =>
              val t = obj.infoType.asInstanceOf[TypeRefType]
              t.symbol.children collect { case c: ClassSymbol =>
                c
              } find (_.symbolInfo.name == clazz.getSimpleName)
            }.head
          }
      }
  }

  private def findConstructor(
      c: ClassSymbol,
      argNames: List[String]
  ): Option[MethodSymbol] = {
    val ms = c.children collect {
      case m: MethodSymbol if m.name == "<init>" => m
    }
    ms.find(m => paramSymbolsFor(m).map(_.name) == argNames)
  }

  private def findField(c: ClassSymbol, name: String): Option[MethodSymbol] =
    (c.children collect {
      case m: MethodSymbol if m.name == name => m
    }).headOption

  // SCALA3 using `?` instead of `_`
  private def findArgType(
      s: MethodSymbol,
      argIdx: Int,
      typeArgIndex: Int
  ): Class[?] = {
    def findPrimitive(t: Type): Symbol = t match {
      case TypeRefType(ThisType(_), symbol, _) if isPrimitive(symbol) => symbol
      case TypeRefType(_, _, TypeRefType(ThisType(_), symbol, _) :: _) =>
        symbol
      case TypeRefType(_, symbol, Nil) => symbol
      case TypeRefType(_, _, args) if typeArgIndex >= args.length =>
        findPrimitive(args(0))
      case TypeRefType(_, _, args) =>
        args(typeArgIndex) match {
          case ref @ TypeRefType(_, _, _) => findPrimitive(ref)
          case x => Meta.fail("Unexpected type info " + x)
        }
      case x => Meta.fail("Unexpected type info " + x)
    }
    toClass(
      findPrimitive(
        paramSymbolsFor(s)(argIdx).asInstanceOf[SymbolInfoSymbol].infoType
      )
    )
  }

  // SCALA3 using `?` instead of `_`
  private def findArgTypeForField(
      s: MethodSymbol,
      typeArgIdx: Int
  ): Class[?] = {
    val t = s.infoType match {
      case NullaryMethodType(TypeRefType(_, _, args)) => args(typeArgIdx)
    }

    @scala.annotation.tailrec
    def findPrimitive(t: Type): Symbol = t match {
      case TypeRefType(ThisType(_), symbol, _) => symbol
      case ref @ TypeRefType(_, _, _)          => findPrimitive(ref)
      case x => Meta.fail("Unexpected type info " + x)
    }
    toClass(findPrimitive(t))
  }

  private def toClass(s: Symbol) = s.path match {
    case "scala.Short"   => classOf[Short]
    case "scala.Int"     => classOf[Int]
    case "scala.Long"    => classOf[Long]
    case "scala.Boolean" => classOf[Boolean]
    case "scala.Float"   => classOf[Float]
    case "scala.Double"  => classOf[Double]
    case _               => classOf[AnyRef]
  }

  private def isPrimitive(s: Symbol) = toClass(s) != classOf[AnyRef]

  // SCALA3 using `?` instead of `_`
  private def findScalaSig(clazz: Class[?]): Option[ScalaSig] =
    ScalaSigParser.parse(clazz).orElse(findScalaSig(clazz.getDeclaringClass))
}
