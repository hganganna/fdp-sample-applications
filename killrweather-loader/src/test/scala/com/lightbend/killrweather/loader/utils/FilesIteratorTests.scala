package com.lightbend.killrweather.loader.utils

import org.scalatest.{ Matchers, WordSpec }
import ResourceLocator._

import scala.io.Source

class FilesIteratorTests extends WordSpec with Matchers {
  "FilesIterator" should {
    "iterate through all matching files in a directory" in {
      val resources = locate("samples")
      val fileData = Seq("samples/sample1.csv", "samples/sample2.csv", "samples/sample3.csv").map(locate(_)).map(Source.fromFile(_).getLines()).flatten
      val zipData = ZipFileIterator(locate("samples/sample-1-2-csv.zip"))
      val allData = fileData ++ zipData
      FilesIterator(resources, "UTF-8").size should be(allData.size)
    }
  }

}
