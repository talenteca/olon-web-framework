package olon.mongodb

import java.util.regex.Pattern

// only i,x,m,s options are supported: https://docs.mongodb.com/manual/reference/operator/query/regex/
object PatternHelper {
  // values
  // 128 -> Pattern.CANON_EQ
  // 2 -> Pattern.CASE_INSENSITIVE
  // 4 -> Pattern.COMMENTS
  // 32 -> Pattern.DOTALL
  // 16 -> Pattern.LITERAL
  // 8 -> Pattern.MULTILINE
  // 64 -> Pattern.UNICODE_CASE
  // 1 -> Pattern.UNIX_LINES

  private val flagMap = Map(
    Pattern.CANON_EQ -> "c",
    Pattern.CASE_INSENSITIVE -> "i",
    Pattern.COMMENTS -> "x",
    Pattern.DOTALL -> "s",
    Pattern.LITERAL -> "t",
    Pattern.MULTILINE -> "m",
    Pattern.UNICODE_CASE -> "u",
    Pattern.UNIX_LINES -> "d"
  )

  def flagsToString(flags: Int): String = {
    (for {
      (mask, char) <- flagMap
      if (flags & mask) != 0
    } yield char).mkString
  }

  def optionsToFlags(opts: String): Int = {
    opts.foldLeft(0) { (result, char) => char match {
      case 'i' => result | Pattern.CASE_INSENSITIVE
      case 'x' => result | Pattern.COMMENTS
      case 'm' => result | Pattern.MULTILINE
      case 's' => result | Pattern.DOTALL
      case _ => result
    } }
  }
}