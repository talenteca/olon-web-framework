package olon
package util

import Helpers._
import common._

// This object holds string constants in a central place
private[olon] object VarConstants {
  val varPrefix = "_lift_sv_"
  val initedSuffix = "_inited_?"
  val lockSuffix = "_lock_dude"
}

trait HasCalcDefaultValue[T] {
  protected def calcDefaultValue: T
}

trait MemoizeVar[K, V] {
  // SCALA3 Using `?` instead of `_`
  protected def coreVar: AnyVar[LRU[K, Box[V]], ?]

  protected def buildLRU = new LRU[K, Box[V]](cacheSize)

  /** The number of entries that will be memoized
    */
  protected def cacheSize: Int = 200

  def apply(key: K): Box[V] = get(key)

  def apply(key: K, dflt: => V): V = get(key, dflt)

  /** Use the MemoizeVar in an extractor
    */
  def unapply(key: K): Option[V] = get(key)

  def get(key: K): Box[V] = coreVar.doSync {
    coreVar.is.get(key) match {
      case Full(x) => x
      case _ => {
        val ret = defaultFunction(key)
        coreVar.is.update(key, ret)
        ret
      }
    }
  }

  /** Override this method if there's a default way of calculating this
    * MemoizedVar (for example, a database lookup)
    */
  protected def defaultFunction(key: K): Box[V] = Empty

  def get(key: K, dflt: => V): V = coreVar.doSync {
    get(key) match {
      case Full(v) => v
      case _ =>
        val ret = dflt
        set(key, ret)
        ret
    }
  }

  protected def __nameSalt: String = ""

  def set(key: K, value: V): Unit = coreVar.doSync {
    coreVar.is.update(key, Full(value))
  }

  def update(key: K, value: V): Unit = set(key, value)
}

abstract class AnyVar[T, MyType <: AnyVar[T, MyType]](dflt: => T)
    extends AnyVarTrait[T, MyType] {
  self: MyType =>

  protected def calcDefaultValue: T = dflt

}

/** Abstract a request or a session scoped variable.
  */
trait AnyVarTrait[T, MyType <: AnyVarTrait[T, MyType]]
    extends PSettableValueHolder[T]
    with HasCalcDefaultValue[T] {
  self: MyType =>
  protected lazy val name =
    VarConstants.varPrefix + getClass.getName + "_" + __nameSalt
  private lazy val initedKey = name + VarConstants.initedSuffix
  protected def findFunc(name: String): Box[T]
  protected def setFunc(name: String, value: T): Unit
  protected def clearFunc(name: String): Unit

  private def _setFunc(name: String, value: T): Unit = {
    setFunc(name, value)

    val sd = settingDefault_?
    changeFuncs.foreach(f => Helpers.tryo(f(Full(value), sd)))
  }

  private def _clearFunc(name: String): Unit = {
    clearFunc(name)
    changeFuncs.foreach(f => Helpers.tryo(f(Empty, false)))
  }

  protected def wasInitialized(name: String, initedKey: String): Boolean
  private var changeFuncs: List[FuncType] = Nil

  /** The function takes a `Box[T]` (Full if the Var is being set, Empty if it's
    * being cleared) and a Boolean indicating that the set function is setting
    * to the default value.
    */
  type FuncType = (Box[T], Boolean) => Unit

  protected def calcDefaultValue: T

  /** On any change to this Var, invoke the function. Changes are setting the
    * value, clearing the value. There may not be a call if the Var goes out of
    * scope (e.g., a RequestVar at the end of the Request).
    *
    * The function takes a `Box[T]` (Full if the Var is being set, Empty if it's
    * being cleared) and a Boolean indicating that the set function is setting
    * to the default value.
    *
    * The function should execute *very* quickly (e.g., Schedule a function to
    * be executed on a different thread).
    *
    * The function should generally be set in Boot or when a singleton is
    * created.
    *
    * @param f
    *   the function to execute on change
    */
  def onChange(f: FuncType): Unit = {
    changeFuncs ::= f
  }

  /** A non-side-effecting test if the value was initialized
    */
  protected def testWasSet(name: String, initedKey: String): Boolean

  protected def __nameSalt = ""

  /** Keep track of whether we're currently setting the default value
    */
  private val settingDefault = new ThreadGlobal[Boolean]

  protected def settingDefault_? : Boolean = settingDefault.box.openOr(false)

  type CleanUpParam

  /** Different Vars require different mechanisms for synchronization. This
    * method implements the Var specific synchronization mechanism
    */
  def doSync[F](f: => F): F

  /** The current value of the variable
    */
  def is: T = doSync {
    findFunc(name) match {
      case Full(v) => v
      case _ =>
        val ret = calcDefaultValue
        testInitialized
        settingDefault.doWith(true) {
          apply(ret)
        }
        // Use findFunc so that we clear the "unread" flag
        findFunc(name) match {
          case Full(v) => v
          case _       => ret
        }
    }
  }

  private def testInitialized: Unit = doSync {
    if (!wasInitialized(name, initedKey)) {
      // SCALA3 removing `_` for function pass as a value
      registerCleanupFunc(_onShutdown)
    }
  }

  /** Shadow of the 'is' method
    */
  def get: T = is

  /** Shadow of the apply method
    */
  def set(what: T): T = apply(what)

  /** Has this Var been set or accessed and had its default value calculated
    */
  def set_? : Boolean = testWasSet(name, initedKey)

  /** Set the Var if it has not been calculated
    */
  def setIfUnset(value: => T): T = doSync {
    if (!set_?) {
      set(value)
    }
    this.is
  }

  /** Set the session variable
    *
    * @param what
    *   -- the value to set the session variable to
    */
  def apply(what: T): T = {
    testInitialized
    _setFunc(name, what)

    what
  }

  /** Applies the given function to the contents of this variable and sets the
    * variable to the resulting value.
    *
    * @param f
    *   -- the function to apply and set the result from.
    */
  def update(f: T => T): T = {
    apply(f(is))
    is
  }

  def remove(): Unit = {
    _clearFunc(name)

  }

  // def cleanupFunc: Box[() => Unit] = Empty

  protected def registerCleanupFunc(in: CleanUpParam => Unit): Unit

  protected final def registerGlobalCleanupFunc(
      in: CleanUpParam => Unit
  ): Unit = {
    cuf ::= in
  }

  private var cuf: List[CleanUpParam => Unit] = Nil

  private def _onShutdown(session: CleanUpParam): Unit = {
    cuf.foreach(f => tryo(f(session)))
    onShutdown(session)
  }

  protected def onShutdown(session: CleanUpParam): Unit = {}

  override def toString = is.toString

  /** Change the value of the Var for the lifespan of the function
    */
  def doWith[F](newVal: T)(f: => F): F = {
    val old = findFunc(name)
    _setFunc(name, newVal)
    try {
      f
    } finally {
      old match {
        case Full(t) => _setFunc(name, t)
        case _       => _clearFunc(name)
      }
    }
  }
}

abstract class NonCleanAnyVar[T](dflt: => T)
    extends AnyVar[T, NonCleanAnyVar[T]](dflt) {
  type CleanUpParam = Unit
  override protected def registerCleanupFunc(in: Unit => Unit): Unit = {}
}

object AnyVar {
  // SCALA3 Using `?` instead of `_`
  implicit def whatVarIs[T](in: AnyVar[T, ?]): T = in.is
}
