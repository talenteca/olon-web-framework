package olon.http

import olon.common.Loggable
import olon.util.HasCalcDefaultValue
import olon.util.LiftValue

object LiftRulesGuardedSetting {
  type StackTrace = Array[StackTraceElement]

  /** Base class for all possible violations which LiftRulesGuardedSetting warns
    * you about.
    * @param settingName
    *   the name of the LiftRules setting which was violated.
    * @param stackTrace
    *   the stacktrace from where the violation occurred
    * @param message
    *   an English message for the developer detailing the violation
    */
  abstract class SettingViolation(
      settingName: String,
      stackTrace: StackTrace,
      message: String
  ) extends Serializable
      with Loggable {

    /** Converts this violation into an Exception which can be handed to a
      * logger for clean message printing
      */
    def toException: Exception = {
      logger.trace("Converting to exception " + settingName)
      val e = new Exception(message)
      e.setStackTrace(stackTrace)
      e
    }
  }

  /** Indicates that a LiftRulesGuardedSetting was written after it had already
    * been read.
    */
  case class SettingWrittenAfterRead(
      settingName: String,
      stackTrace: StackTrace,
      message: String
  ) extends SettingViolation(settingName, stackTrace, message)

  /** Indicates that a LiftRulesGuardedSetting was written after Lift finished
    * booting.
    */
  case class SettingWrittenAfterBoot(
      settingName: String,
      stackTrace: StackTrace,
      message: String
  ) extends SettingViolation(settingName, stackTrace, message)
}

import LiftRulesGuardedSetting._

/** This class encapsulates a mutable LiftRules setting which guards its value
  * against changes which can produce unexpected results in a Lift application.
  *
  * @param name
  *   the name of the LiftRules setting (an unfortunate duplication of the name
  *   given on LiftRules itself).
  * @param default
  *   the default value of this setting
  * @tparam T
  *   the type of the setting
  */
class LiftRulesGuardedSetting[T](val name: String, val default: T)
    extends LiftValue[T]
    with HasCalcDefaultValue[T] {
  // SCALA3 Using `private` instead of `private[this]`
  private var v: T = default
  private var lastSet: Option[StackTrace] = None
  private var lastRead: Option[StackTrace] = None

  // SCALA3 Using `private` instead of `private[this]`
  private def writeAfterReadMessage =
    s"LiftRules.$name was set after already being read! " +
      s"Review the stacktrace below to see where the value was last read. "

  // SCALA3 Using `private` instead of `private[this]`
  private def writeAfterBootMessage =
    s"LiftRules.$name set after Lift finished booting. " +
      s"Review the stacktrace below to see where settings are being changed after boot time. "

  // SCALA3 Using `private` instead of `private[this]`
  private def trimmedStackTrace(t: Throwable): StackTrace = {
    val toIgnore = Set("LiftRulesGuardedSetting", "LiftValue")
    t.getStackTrace.dropWhile(e =>
      toIgnore.find(e.getClassName.contains(_)).isDefined
    )
  }

  // SCALA3 Using `private` instead of `private[this]`
  private def currentStackTrace: StackTrace = trimmedStackTrace(
    new Exception
  )

  override def set(value: T): T = {
    if (LiftRules.doneBoot) {
      val e =
        SettingWrittenAfterBoot(name, currentStackTrace, writeAfterBootMessage)
      LiftRules.guardedSettingViolationFunc.get.apply(e)
    }

    // TODO: Skip if the value is the same?
    lastRead.foreach { stackTrace =>
      val e2 = SettingWrittenAfterRead(name, stackTrace, writeAfterReadMessage)
      LiftRules.guardedSettingViolationFunc.get.apply(e2)
    }

    lastSet = Some(currentStackTrace)
    v = value
    v
  }

  override def get: T = {
    if (!LiftRules.doneBoot) lastRead = Some(currentStackTrace)
    v
  }

  override protected def calcDefaultValue: T = default
}
