package com.tantalim.database.data

import com.tantalim.models.DataType
import org.junit.runner.RunWith
import org.specs2.mutable.Specification
import org.specs2.runner.JUnitRunner

@RunWith(classOf[JUnitRunner])
class ComparatorByDataTypeSpec extends Specification {
  "ComparatorByDataType" should {
    "Boolean" in {
      ComparatorByDataType.get(DataType.Boolean) must have length 3
    }
    "Date" in {
      ComparatorByDataType.get(DataType.Date) must have length 7
    }
    "Integer" in {
      ComparatorByDataType.get(DataType.Integer) must have length 7
    }
    "String" in {
      ComparatorByDataType.get(DataType.String) must have length 6
    }
  }
}
