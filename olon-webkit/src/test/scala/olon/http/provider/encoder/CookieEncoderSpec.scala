package olon.http.provider.encoder

import olon.common._
import olon.http.provider._
import org.specs2.mutable.Specification

object CookieEncoderSpec extends Specification {

  "CookieEncoder" should {
    "convert a simple cookie" in {
      val cookie = HTTPCookie("test-name", "test-value")
      CookieEncoder.encode(cookie) must_== "test-name=test-value"
    }

    "convert a secure cookie" in {
      val cookie = HTTPCookie(
        name = "test-name",
        value = Full("test-value"),
        domain = Empty,
        path = Empty,
        maxAge = Empty,
        secure_? = Full(true),
        httpOnly = Empty,
        sameSite = Empty
      )
      CookieEncoder.encode(cookie) must_== "test-name=test-value; Secure"
    }

    "convert a cookie with a domain" in {
      val cookie = HTTPCookie(
        name = "test-name",
        value = Full("test-value"),
        domain = Full("test-domain.com"),
        path = Empty,
        maxAge = Empty,
        secure_? = Empty,
        httpOnly = Empty,
        sameSite = Empty
      )
      CookieEncoder.encode(
        cookie
      ) must_== "test-name=test-value; Domain=test-domain.com"
    }

    "convert a cookie with a path" in {
      val cookie = HTTPCookie(
        name = "test-name",
        value = Full("test-value"),
        domain = Empty,
        path = Full("/test-path"),
        maxAge = Empty,
        secure_? = Empty,
        httpOnly = Empty,
        sameSite = Empty
      )
      CookieEncoder.encode(
        cookie
      ) must_== "test-name=test-value; Path=/test-path"
    }

    "convert a cookie with a max age" in {
      val cookie = HTTPCookie(
        name = "test-name",
        value = Full("test-value"),
        domain = Empty,
        path = Empty,
        maxAge = Full(10),
        secure_? = Empty,
        httpOnly = Empty,
        sameSite = Empty
      )
      val encodedCookie = CookieEncoder.encode(cookie)
      encodedCookie.startsWith("test-name=test-value; ") must_== true
      encodedCookie.contains("Max-Age=10; Expires=") must_== true
    }

    "convert an HTTP only cookie" in {
      val cookie = HTTPCookie(
        name = "test-name",
        value = Full("test-value"),
        domain = Empty,
        path = Empty,
        maxAge = Empty,
        secure_? = Empty,
        httpOnly = Full(true),
        sameSite = Empty
      )
      CookieEncoder.encode(cookie) must_== "test-name=test-value; HTTPOnly"
    }

    "convert a same site LAX cookie" in {
      val cookie = HTTPCookie(
        name = "test-name",
        value = Full("test-value"),
        domain = Empty,
        path = Empty,
        maxAge = Empty,
        secure_? = Empty,
        httpOnly = Empty,
        sameSite = Full(SameSite.LAX)
      )
      CookieEncoder.encode(cookie) must_== "test-name=test-value; SameSite=Lax"
    }

    "convert a same site NONE cookie" in {
      val cookie = HTTPCookie(
        name = "test-name",
        value = Full("test-value"),
        domain = Empty,
        path = Empty,
        maxAge = Empty,
        secure_? = Empty,
        httpOnly = Empty,
        sameSite = Full(SameSite.NONE)
      )
      CookieEncoder.encode(cookie) must_== "test-name=test-value; SameSite=None"
    }

    "convert a same site STRICT cookie" in {
      val cookie = HTTPCookie(
        name = "test-name",
        value = Full("test-value"),
        domain = Empty,
        path = Empty,
        maxAge = Empty,
        secure_? = Empty,
        httpOnly = Empty,
        sameSite = Full(SameSite.STRICT)
      )
      CookieEncoder.encode(
        cookie
      ) must_== "test-name=test-value; SameSite=Strict"
    }

    "convert a secure same site none cookie" in {
      val cookie = HTTPCookie(
        name = "test-name",
        value = Full("test-value"),
        domain = Empty,
        path = Empty,
        maxAge = Empty,
        secure_? = Full(true),
        httpOnly = Empty,
        sameSite = Full(SameSite.NONE)
      )
      CookieEncoder.encode(
        cookie
      ) must_== "test-name=test-value; Secure; SameSite=None"
    }

    "convert a secure same site strict cookie with max age" in {
      val cookie = HTTPCookie(
        name = "test-name",
        value = Full("test-value"),
        domain = Empty,
        path = Empty,
        maxAge = Full(10),
        secure_? = Full(true),
        httpOnly = Empty,
        sameSite = Full(SameSite.NONE)
      )
      val encodedCookie = CookieEncoder.encode(cookie)
      encodedCookie.startsWith(
        "test-name=test-value; Max-Age=10; Expires="
      ) must_== true
      encodedCookie.endsWith("; Secure; SameSite=None") must_== true
    }

    "convert a secure same site lax cookie with max age, domain and path" in {
      val cookie = HTTPCookie(
        name = "test-name",
        value = Full("test-value"),
        domain = Full("test-domain.com"),
        path = Full("/test-path"),
        maxAge = Full(10),
        secure_? = Full(true),
        httpOnly = Full(false),
        sameSite = Full(SameSite.LAX)
      )
      val encodedCookie = CookieEncoder.encode(cookie)
      encodedCookie.startsWith(
        "test-name=test-value; Max-Age=10; Expires="
      ) must_== true
      encodedCookie.endsWith(
        "; Path=/test-path; Domain=test-domain.com; Secure; SameSite=Lax"
      ) must_== true
    }

    "convert a secure HTTP only cookie" in {
      val cookie = HTTPCookie(
        name = "test-name",
        value = Full("test-value"),
        domain = Empty,
        path = Empty,
        maxAge = Empty,
        secure_? = Full(true),
        httpOnly = Full(true),
        sameSite = Empty
      )
      CookieEncoder.encode(
        cookie
      ) must_== "test-name=test-value; Secure; HTTPOnly"
    }

    "convert a cookie with only the name" in {
      val cookie = HTTPCookie(
        name = "test-name",
        value = Empty,
        domain = Empty,
        path = Empty,
        maxAge = Empty,
        secure_? = Empty,
        httpOnly = Empty,
        sameSite = Empty
      )
      CookieEncoder.encode(cookie) must_== "test-name="
    }

  }

}
