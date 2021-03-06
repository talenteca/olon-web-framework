package olon
package util

import java.util.Locale
import java.text.{NumberFormat, DecimalFormat}

trait TwoFractionDigits {
    def numberOfFractionDigits = 2
    def scale = 10
}

trait DollarCurrency extends TwoFractionDigits {
    def currencySymbol: String = "$"
}

/* Various Currencies */
object AU extends CurrencyZone {
    type Currency = AUD
    var locale = new Locale("en", "AU")
    def make(x: BigDecimal) = new Currency{def amount = x}
    abstract class AUD extends AbstractCurrency("AUD") with DollarCurrency {}
}

object US extends CurrencyZone {
    type Currency = USD
    var locale = Locale.US
    def make(x: BigDecimal) = new Currency{def amount = x}
    abstract class USD extends AbstractCurrency("USD") with DollarCurrency {}
}

object GB extends CurrencyZone {
    type Currency = GBP
    var locale = Locale.UK
    def make(x: BigDecimal) = new Currency{def amount = x}
    abstract class GBP extends AbstractCurrency("GBP") with TwoFractionDigits {def currencySymbol = "£"}
}

object EU extends CurrencyZone {
    type Currency = EUR
    var locale = Locale.GERMANY // guess this is why its a var
    def make(x: BigDecimal) = new Currency{def amount = x; override val _locale = locale}
    abstract class EUR extends AbstractCurrency("EUR") with TwoFractionDigits {def currencySymbol = "€"}
}

abstract class CurrencyZone {
    type Currency <: AbstractCurrency

    var locale: Locale
    def make(x: BigDecimal): Currency

    def apply(x: String): Currency = {
        try {
            make(BigDecimal(x)) // try normal number
        } catch { case e: java.lang.NumberFormatException => {
                    try {
                        make(BigDecimal(""+NumberFormat.getNumberInstance(locale).parse(x))) // try with grouping separator
                    } catch { case e: java.text.ParseException => {
                                make(BigDecimal(""+NumberFormat.getCurrencyInstance(locale).parse(x))) } // try with currency symbol and grouping separator
                    }
                }
        }

    }

    def apply(x: BigDecimal): Currency = make(x)

    /* currency factory*/
    abstract class AbstractCurrency(val designation: String) extends Ordered[Currency] {

        val _locale: Locale = locale
        def amount: BigDecimal
        def floatValue = amount.floatValue
        def doubleValue = amount.doubleValue
        def currencySymbol: String
        def numberOfFractionDigits: Int
        def scale: Int

        def +(that: Currency): Currency = make(this.amount + that.amount)
        def +(that: Int): Currency = this + make(that)

        def *(that: Currency): Currency = make(this.amount * that.amount)
        def *(that: Int): Currency = this * make(that)

        def -(that: Currency): Currency = make(this.amount - that.amount)
        def -(that: Int): Currency = this - make(that)

        def /(that: Currency): Currency =
        make(new BigDecimal(this.amount.bigDecimal.divide(that.amount.bigDecimal, scale, java.math.BigDecimal.ROUND_HALF_UP)) )
        def /(that: Int): Currency = this / make(that)

        def compare(that: Currency) = this.amount compare that.amount

        override def equals(that: Any) = that match {
            case that: AbstractCurrency => this.designation+this.format("", scale) == that.designation+that.format("", scale)
            case _ => false
        }

        override def hashCode = (this.designation+format("", scale)).hashCode

        def round(precision: Int) = make(BigDecimal(get(precision)))

        override def toString = format("", numberOfFractionDigits)

        def format(fd: Int): String = format(currencySymbol, fd)

        def format: String = format(currencySymbol, numberOfFractionDigits)

        def format(currencySymbol: String, numberOfFractionDigits: Int): String = {
            val moneyValue = amount match {
                case null => 0
                case _ => amount.setScale(numberOfFractionDigits, BigDecimal.RoundingMode.HALF_UP).doubleValue;
            }

            val numberFormat = NumberFormat.getCurrencyInstance(_locale);
            numberFormat.setMinimumFractionDigits(numberOfFractionDigits);
            numberFormat.setMaximumFractionDigits(numberOfFractionDigits);
            val symbol=numberFormat.getCurrency.getSymbol(_locale)
            numberFormat.format(moneyValue).replace(symbol, currencySymbol)

        }

        def get: String = get(numberOfFractionDigits)

        def get(numberOfFractionDigits: Int): String = {
            val nf = NumberFormat.getNumberInstance(_locale)
            val df = nf.asInstanceOf[DecimalFormat]
            val groupingSeparator = df.getDecimalFormatSymbols.getGroupingSeparator

            format("", numberOfFractionDigits).replaceAll(groupingSeparator+"", "");
        }

    }

}
