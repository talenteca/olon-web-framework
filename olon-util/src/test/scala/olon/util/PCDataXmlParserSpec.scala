package olon
package util

import org.specs2.matcher.XmlMatchers
import org.specs2.mutable.Specification

/** Systems under specification for PCDataXmlParser.
  */
class PCDataXmlParserSpec extends Specification with XmlMatchers {
  "PCDataXmlParser Specification".title
  val data1 = """


<html>dude</html>


"""

  val data2 = """

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html>dude</html>


"""

  val data3 = """<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en" lang="en">
<head>
<title>Meow</title>
<meta name="generator" content="Bluefish 1.0.7"/>
<meta name="author" content="David Pollak"/>
<meta name="date" content="2010-10-31T05:40:15-0700"/>
<meta name="copyright" content=""/>
<meta name="keywords" content=""/>
<meta name="description" content=""/>
<meta name="ROBOTS" content="NOINDEX, NOFOLLOW"/>
<meta http-equiv="content-type" content="text/html; charset=UTF-8"/>
<meta http-equiv="content-type" content="application/xhtml+xml; charset=UTF-8"/>
<meta http-equiv="content-style-type" content="text/css"/>
</head>
<body>

</body>
</html>"""

  "PCDataMarkupParser" should {
    "Parse a document with whitespace" in {
      PCDataXmlParser(data1).openOrThrowException("Test") must ==/(
        <html>dude</html>
      )
    }

    "Parse a document with doctype" in {
      PCDataXmlParser(data2).openOrThrowException("Test") must ==/(
        <html>dude</html>
      )
    }

    "Parse a document with xml and doctype" in {
      PCDataXmlParser(data3)
        .openOrThrowException("Test")
        .apply(0)
        .label must_== "html"
    }

  }

}
