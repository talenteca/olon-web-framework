package olon
package json

import org.specs2.mutable.Specification

object MergeExamples extends Specification {
  "Merge Examples".title

  "Merge example" in {
    (scala1.merge(scala2)) mustEqual expectedMergeResult
  }

  val scala1 = parse("""
    {
      "lang": "scala",
      "year": 2006,
      "tags": ["fp", "oo"],
      "features": {
        "key1":"val1",
        "key2":"val2" 
      }
    }""")

  val scala2 = parse("""
    {
      "tags": ["static-typing","fp"],
      "compiled": true,
      "lang": "scala",
      "features": {
        "key2":"newval2",
        "key3":"val3"
      }
    }""")

  val expectedMergeResult = parse("""
    {
      "lang": "scala",
      "year": 2006,
      "tags": ["fp", "oo", "static-typing"],
      "features": {
        "key1":"val1",
        "key2":"newval2",
        "key3":"val3"
      },
      "compiled": true,
    }""")

  "Lotto example" in {
    (lotto1.merge(lotto2)) mustEqual mergedLottoResult
  }

  val lotto1 = parse("""
    {
      "lotto":{
        "lotto-id":5,
        "winning-numbers":[2,45,34,23,7,5,3]
        "winners":[{
          "winner-id":23,
          "numbers":[2,45,34,23,3,5]
        }]
      }
    }""")

  val lotto2 = parse("""
    {
      "lotto":{ 
        "winners":[{
          "winner-id":54,
          "numbers":[52,3,12,11,18,22]
        }]
      }
    }""")

  val mergedLottoResult = parse("""
    {
      "lotto":{
        "lotto-id":5,
        "winning-numbers":[2,45,34,23,7,5,3],
        "winners":[{
          "winner-id":23,
          "numbers":[2,45,34,23,3,5]
        },{
          "winner-id":54,
          "numbers":[52,3,12,11,18,22]
        }]
      }
    }""")
}
