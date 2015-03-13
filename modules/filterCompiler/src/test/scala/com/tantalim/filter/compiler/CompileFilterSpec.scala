package com.tantalim.filter.compiler

import com.tantalim.models.{FakeArtifacts, DataType, ModelField}
import org.joda.time.DateTime
import org.junit.runner.RunWith
import org.specs2.mutable._
import org.specs2.runner._

@RunWith(classOf[JUnitRunner])
class CompileFilterSpec extends Specification with FakeArtifacts {

  val modelColumnSample: Map[String, ModelField] = Map(
    fakeModelFieldMap("TableID", "id", DataType.Integer, None, false, true),
    fakeModelFieldMap("CreatedDate", "created_date", DataType.Date),
    fakeModelFieldMap("TableName", "name", DataType.String)
  )

  private def mustBeEqual(filterString: String, whereClause: String, parameters: List[Any]) = {
    val filter: CompileFilter = new CompileFilter(filterString, modelColumnSample)
    filter.parse() must be equalTo CompiledFilter(whereClause, parameters)
  }

  "Filter" should {
    "filter strings" in {
      "=" in {
        mustBeEqual("TableName = 'Person'", "`t0`.`name` = ?", List("Person"))
      }

      "Equals" in {
        mustBeEqual("TableName Equals 'Person'", "`t0`.`name` = ?", List("Person"))
      }

      "BeginsWith" in {
        mustBeEqual("TableName BeginsWith 'Person'", "`t0`.`name` LIKE ?", List("Person%"))
      }

      "EndsWith" in {
        mustBeEqual("TableName EndsWith 'Person'", "`t0`.`name` LIKE ?", List("%Person"))
      }

      "Contains" in {
        mustBeEqual("TableName Contains 'Person'", "`t0`.`name` LIKE ?", List("%Person%"))
      }
    }
    "filter numbers" in {
      "=" in {
        mustBeEqual("TableID > 3", "`t0`.`id` > ?", List(3))
      }
      "In" in {
        mustBeEqual("TableID In (1, 2)", "`t0`.`id` IN (?,?)", List(1, 2))
      }
    }
    "filter dates" in {
      "on or after date" in {
        mustBeEqual("CreatedDate OnOrAfter '2000-01-01'", "`t0`.`created_date` >= ?", List(DateTime.parse("2000-01-01")))
      }
      "before" in {
        mustBeEqual("CreatedDate Before NOW", "`t0`.`created_date` < NOW()", List.empty)
      }
      "more than 2 days from now" in {
        mustBeEqual("CreatedDate After 2D", "`t0`.`created_date` > DATE_ADD(NOW(), INTERVAL 2 DAY)", List.empty)
      }
      "after 12 months ago" in {
        mustBeEqual("CreatedDate After -12M", "`t0`.`created_date` > DATE_SUB(NOW(), INTERVAL 12 MONTH)", List.empty)
      }
    }
    "filter complex phrases" in {
      "and" in {
        mustBeEqual("TableID > 1 AND TableID < 9", "`t0`.`id` > ? AND `t0`.`id` < ?", List(1, 9))
      }
      "or" in {
        mustBeEqual("TableName = 'Person' OR TableName Equals 'Human'", "`t0`.`name` = ? OR `t0`.`name` = ?", List("Person", "Human"))
      }
    }
  }
}