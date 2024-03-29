package olon
package util

import java.io._
import scala.language.postfixOps
import scala.util.parsing.combinator._

import common._

object CssHelpers extends ControlHelpers {

  /** Adds a prefix to root relative paths in the url segments from the css
    * content
    *
    * @param in
    *   \- the text reader
    * @param rootPrefix
    *   \- the prefix to be added
    * @return
    *   (Box[String], String) - returns the tuple containing the parsing output
    *   and the original input (as a String)
    */
  def fixCSS(in: Reader, rootPrefix: String): (Box[String], String) = {
    val reader = new BufferedReader(in)
    val res = new StringBuilder;
    var line: String = null;
    try {
      while ({ line = reader.readLine(); line != null }) {
        res append line + "\n"
      }
    } finally {
      reader close
    }
    val str = res toString;
    (CssUrlPrefixer(rootPrefix).fixCss(str), str);
  }
}

/** Utility for prefixing root-relative `url`s in CSS with a given prefix.
  * Typically used to prefix root-relative CSS `url`s with the application
  * context path.
  *
  * After creating the prefixer with the prefix you want to apply to
  * root-relative paths, call `fixCss` with a CSS string to return a fixed CSS
  * string.
  */
case class CssUrlPrefixer(prefix: String) extends Parsers {
  implicit def strToInput(in: String): Input =
    new scala.util.parsing.input.CharArrayReader(in.toCharArray)
  type Elem = Char

  lazy val contentParser = Parser[String] { case in =>
    val content = new StringBuilder;
    var seqDone = 0;

    def walk(in: Input)(f: Char => Boolean): Input = {
      var seq = in
      while (!seq.atEnd && !f(seq first)) {
        seq = seq rest
      }
      seq rest
    }

    val rest = walk(in) { case c =>
      content append c
      c.toLower match {
        case 'u' if (seqDone == 0) => seqDone = 1;
        case 'r' if (seqDone == 1) => seqDone = 2;
        case 'l' if (seqDone == 2) => seqDone = 3;
        case ' ' | '\t' | '\n' | '\r' if (seqDone == 3 || seqDone == 4) =>
          seqDone = 4
        case '(' if (seqDone == 3 || seqDone == 4) => seqDone = 5
        case _                                     => seqDone = 0
      }
      seqDone == 5;
    }

    Success(content toString, rest);
  }

  lazy val spaces = (elem(' ') | elem('\t') | elem('\n') | elem('\r')).*

  def pathWith(additionalCharacters: Char*) = {
    elem(
      "path",
      c =>
        c.isLetterOrDigit ||
          c == '?' || c == '/' ||
          c == '&' || c == '@' ||
          c == ';' || c == '.' ||
          c == '+' || c == '-' ||
          c == '=' || c == ':' ||
          c == ' ' || c == '_' ||
          c == '#' || c == ',' ||
          c == '%' || additionalCharacters.contains(c)
    ).+ ^^ { case l =>
      l.mkString("")
    }
  }

  // consider only root relative paths that start with /
  lazy val path = pathWith()

  def fullUrl(innerUrl: Parser[String], quoteString: String): Parser[String] = {
    val escapedPrefix =
      if (quoteString.isEmpty) {
        prefix
      } else {
        prefix.replace(quoteString, "\\" + quoteString)
      }

    // do the parsing per CSS spec http://www.w3.org/TR/REC-CSS2/syndata.html#uri section 4.3.4
    spaces ~> innerUrl <~ (spaces <~ elem(')')) ^^ {
      case urlPath => {
        val trimmedPath = urlPath.trim

        val updatedPath =
          if (trimmedPath.charAt(0) == '/') {
            escapedPrefix + trimmedPath
          } else {
            trimmedPath
          }

        quoteString + updatedPath + quoteString + ")"
      }
    }
  }

  // the URL might be wrapped in simple quotes
  lazy val singleQuotedPath =
    fullUrl(elem('\'') ~> pathWith('"') <~ elem('\''), "'")
  // the URL might be wrapped in double quotes
  lazy val doubleQuotedPath =
    fullUrl(elem('\"') ~> pathWith('\'') <~ elem('\"'), "\"")
  // the URL might not be wrapped at all
  lazy val quotelessPath = fullUrl(path, "")

  lazy val phrase =
    (((contentParser ~ singleQuotedPath) |||
      (contentParser ~ doubleQuotedPath) |||
      (contentParser ~ quotelessPath)).* ^^ { case l =>
      l.flatMap(f => f._1 + f._2).mkString("")
    }) ~ contentParser ^^ { case a ~ b =>
      a + b
    }

  def fixCss(cssString: String): Box[String] = {
    phrase(cssString) match {
      case Success(updatedCss, remaining) if remaining.atEnd =>
        Full(updatedCss)

      case Success(_, remaining) =>
        val remainingString =
          remaining.source
            .subSequence(
              remaining.offset,
              remaining.source.length
            )
            .toString

        common.Failure(
          s"Parser did not consume all input. Parser error? Unconsumed:\n$remainingString"
        )

      case failure =>
        common.Failure(s"Parse failed with result $failure") ~> failure
    }
  }

}
