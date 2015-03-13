package com.tantalim.script.compiler

import com.tantalim.models.{DataType, ModelField}
import org.junit.runner.RunWith
import org.specs2.mutable._
import org.specs2.runner._

@RunWith(classOf[JUnitRunner])
class CompileScriptSpec extends Specification {
  "Filter" should {
    "filter strings" in {
      "=" in {

        "a" must be equalTo "a"
      }
    }
  }
}
