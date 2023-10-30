package olon
package util

import org.specs2.mutable.Specification

import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Path

import common._
import Helpers._

class IoHelpersSpec extends Specification with IoHelpers {
  "IoHelpers Specification".title

  "Io helpers" should {

    "readWholeFile properly" in {
      // Copy a resource file to the tmp directory so we can refer to it as a Path
      val resourceAsPath: Box[Path] = {
        for {
          bytes <- tryo(
            readWholeStream(getClass.getResourceAsStream("IoHelpersSpec.txt"))
          ).filter(_ ne null)
          text <- tryo(new String(bytes))
          path = {
            val tempFile =
              Files.createTempFile(s"IoHelpersSpec_${nextFuncName}", ".tmp")
            Files.write(tempFile, text.getBytes(StandardCharsets.UTF_8))
            tempFile
          }
        } yield path
      }

      resourceAsPath.isDefined must_== true

      resourceAsPath.foreach { path =>
        val pathContents = new String(readWholeFile(path)).trim
        Files.delete(path)
        pathContents must_== "IoHelpersSpec"
      }

      success
    }
  }
}
