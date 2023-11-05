package olon
package http
package provider
package encoder

import olon.http.provider.HTTPCookie
import olon.http.provider.SameSite

import java.util._

/** Converts an HTTPCookie into a string to used as header cookie value.
  *
  * The string representation follows the <a
  * href="https://tools.ietf.org/html/rfc6265">RFC6265</a> standard with the
  * added field of SameSite to support secure browsers as explained at <a
  * href="https://developer.mozilla.org/en-US/docs/Web/HTTP/Headers/Set-Cookie/SameSite">MDN
  * SameSite Cookies</a>
  *
  * This code is based on the Netty's HTTP cookie encoder.
  *
  * Multiple cookies are supported just sending separate "Set-Cookie" headers
  * for each cookie.
  */
object CookieEncoder {

  private val PATH = "Path"

  private val EXPIRES = "Expires"

  private val MAX_AGE = "Max-Age"

  private val DOMAIN = "Domain"

  private val SECURE = "Secure"

  private val HTTPONLY = "HTTPOnly"

  private val SAMESITE = "SameSite"

  private val DAY_OF_WEEK_TO_SHORT_NAME =
    Array("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat")

  private val CALENDAR_MONTH_TO_SHORT_NAME = Array(
    "Jan",
    "Feb",
    "Mar",
    "Apr",
    "May",
    "Jun",
    "Jul",
    "Aug",
    "Sep",
    "Oct",
    "Nov",
    "Dec"
  )

  def encode(cookie: HTTPCookie): String = {
    val name = cookie.name
    val value = cookie.value.getOrElse("")
    val buf = new StringBuilder()
    add(buf, name, value);
    cookie.maxAge foreach { maxAge =>
      add(buf, MAX_AGE, maxAge);
      val expires = new Date(maxAge * 1000 + System.currentTimeMillis());
      buf.append(EXPIRES);
      buf.append('=');
      appendDate(expires, buf);
      buf.append(';');
      buf.append(' ');
    }
    cookie.path foreach { path =>
      add(buf, PATH, path);
    }
    cookie.domain foreach { domain =>
      add(buf, DOMAIN, domain);
    }
    cookie.secure_? foreach { isSecure =>
      if (isSecure) add(buf, SECURE);
    }
    cookie.httpOnly foreach { isHttpOnly =>
      if (isHttpOnly) add(buf, HTTPONLY)
    }
    cookie.sameSite foreach {
      case SameSite.LAX =>
        add(buf, SAMESITE, "Lax")
      case SameSite.STRICT =>
        add(buf, SAMESITE, "Strict")
      case SameSite.NONE =>
        add(buf, SAMESITE, "None")
    }
    stripTrailingSeparator(buf)
  }

  private def appendDate(date: Date, sb: StringBuilder): StringBuilder = {
    val cal = new GregorianCalendar(TimeZone.getTimeZone("UTC"))
    cal.setTime(date)
    sb.append(DAY_OF_WEEK_TO_SHORT_NAME(cal.get(Calendar.DAY_OF_WEEK) - 1))
      .append(", ")
    appendZeroLeftPadded(cal.get(Calendar.DAY_OF_MONTH), sb).append(' ')
    sb.append(CALENDAR_MONTH_TO_SHORT_NAME(cal.get(Calendar.MONTH))).append(' ')
    sb.append(cal.get(Calendar.YEAR)).append(' ')
    appendZeroLeftPadded(cal.get(Calendar.HOUR_OF_DAY), sb).append(':')
    appendZeroLeftPadded(cal.get(Calendar.MINUTE), sb).append(':')
    appendZeroLeftPadded(cal.get(Calendar.SECOND), sb).append(" GMT")
  }

  private def appendZeroLeftPadded(
      value: Int,
      sb: StringBuilder
  ): StringBuilder = {
    if (value < 10) {
      sb.append('0');
    }
    return sb.append(value);
  }

  private def stripTrailingSeparator(buf: StringBuilder) = {
    if (buf.length > 0) {
      buf.setLength(buf.length - 2);
    }
    buf.toString()
  }

  private def add(sb: StringBuilder, name: String, value: Long) = {
    sb.append(name);
    sb.append('=');
    sb.append(value);
    sb.append(';');
    sb.append(' ');
  }

  private def add(sb: StringBuilder, name: String, value: String) = {
    sb.append(name);
    sb.append('=');
    sb.append(value);
    sb.append(';');
    sb.append(' ');
  }

  private def add(sb: StringBuilder, name: String) = {
    sb.append(name);
    sb.append(';');
    sb.append(' ');
  }

}
