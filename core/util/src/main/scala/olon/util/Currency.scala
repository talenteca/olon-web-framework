package olon
package util

import Helpers._
import common._

/**
 * A simple fixed-point currency representation
 */
class Currency(val amount: Long, val symbol: String, val decimals: Int) {
  override def toString = {
    if (decimals == 0) symbol+amount
    else {
      val d = amount.toDouble
      val pow = math.pow(10, decimals)
      symbol+(d / pow)
    }
  }

  /**
   * Return a string formatted as the URL-encoded symbol followed
   * by the amount and decimals delimited by the "&amp;" symbol.
   */
  def forDB: String = Helpers.urlEncode(symbol)+"&"+amount+"&"+decimals

  /**
   * Determines whether two currencies are equal with respect to
   * symbol, amount, and decimal value.
   */
  override def equals(other: Any) = other match {
    case c: Currency => c.amount == amount && c.symbol == symbol && c.decimals == decimals
    case _ => false
  }

  /**
   * Addition on Currency objects. This compares currency symbols to prevent
   * addition of different types of currency to one another.
   * @throws CurrencyMismatchException for mismatched currency types.
   */
  def +(other: Currency): Currency =
  if (symbol != other.symbol || decimals != other.decimals) throw new CurrencyMismatchException
  else new Currency(amount + other.amount, symbol, decimals)

  /**
   * Subtraction on Currency objects. This compares currency symbols to prevent
   * subtraction of different types of currency from one another.
   * @throws CurrencyMismatchException for mismatched currency types.
   */
  def -(other: Currency): Currency =
  if (symbol != other.symbol || decimals != other.decimals) throw new CurrencyMismatchException
  else new Currency(amount - other.amount, symbol, decimals)
}

/**
 * This exception is thrown if an operation is attempted on two currency values
 * where currency symbols do not match.
 */
class CurrencyMismatchException extends Exception

object Currency {
  /**
   * Parse a currency from the format specified by Currency.forDB
   */
  def apply(s: String): Box[Currency] = s.roboSplit("&") match {
    case List(cur, a, d) => for (av <- asLong(a); dv <- asInt(d)) yield new Currency(av, urlDecode(cur), dv)
    case _ => Empty
  }
}
