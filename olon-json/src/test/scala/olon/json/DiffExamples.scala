package olon
package json

import org.specs2.mutable.Specification

object DiffExamples extends Specification {
  import MergeExamples.{scala1, scala2, lotto1, lotto2, mergedLottoResult}

  "Diff example" in {
    val Diff(changed, added, deleted) = scala1 diff scala2
    (changed mustEqual expectedChanges) and
      (added mustEqual expectedAdditions) and
      (deleted mustEqual expectedDeletions)
  }

  val expectedChanges = parse("""
    {
      "tags": ["static-typing","fp"],
      "features": {
        "key2":"newval2"
      }
    }""")

  val expectedAdditions = parse("""
    {
      "features": {
        "key3":"val3"
      },
      "compiled": true
    }""")

  val expectedDeletions = parse("""
    {
      "year":2006,
      "features":{ "key1":"val1" }
    }""")

  "Lotto example" in {
    val Diff(changed, added, deleted) = mergedLottoResult diff lotto1
    (changed mustEqual JNothing) and
      (added mustEqual JNothing) and
      (deleted mustEqual lotto2)
  }

  "Example from http://tlrobinson.net/projects/js/jsondiff/" in {
    val json1 = read("/diff-example-json1.json")
    val json2 = read("/diff-example-json2.json")
    val expectedChanges = read("/diff-example-expected-changes.json")
    val expectedAdditions = read("/diff-example-expected-additions.json")
    val expectedDeletions = read("/diff-example-expected-deletions.json")

    json1 diff json2 mustEqual Diff(
      expectedChanges,
      expectedAdditions,
      expectedDeletions
    )
  }

  private def read(resource: String) =
    parse(
      scala.io.Source
        .fromInputStream(getClass.getResourceAsStream(resource))
        .getLines()
        .mkString
    )
}
