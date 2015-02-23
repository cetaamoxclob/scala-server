package data

import java.sql.Date
import java.util.Calendar

import mock.FakeArtifacts
import models.{TableColumn, ModelField}
import org.joda.time.DateTime
import org.junit.runner.RunWith
import org.specs2.mutable._
import org.specs2.runner._

@RunWith(classOf[JUnitRunner])
class DataFilterSpec extends Specification with FakeArtifacts {

  val modelColumnSample: Map[String, ModelField] = Map(
    fakeModelFieldMap("TableID", "id", "Integer", None, false, true),
    fakeModelFieldMap("CreatedDate", "created_date", "Date"),
    fakeModelFieldMap("TableName", "name", "String")
  )

  private def mustBeEqual(filter: String, whereClause: String, parameters: List[Any]) = {
    DataFilter.parse(filter, modelColumnSample) must be equalTo(whereClause, parameters)
  }

  "DataFilter" should {
    "filter strings" in {
      "=" in {
        mustBeEqual("TableName = Person", "`t0`.`name` = ?", List("Person"))
      }

      "Equals" in {
        mustBeEqual("TableName Equals Person", "`t0`.`name` = ?", List("Person"))
      }

      "BeginsWith" in {
        mustBeEqual("TableName BeginsWith Person", "`t0`.`name` LIKE ?", List("Person%"))
      }

      "EndsWith" in {
        mustBeEqual("TableName EndsWith Person", "`t0`.`name` LIKE ?", List("%Person"))
      }

      "Contains" in {
        mustBeEqual("TableName Contains Person", "`t0`.`name` LIKE ?", List("%Person%"))
      }
    }

    "filter numbers" in {
      "In" in {
        mustBeEqual("TableID In (1,2)", "`t0`.`id` IN (?,?)", List(1, 2))
      }
    }

    "filter dates" in {
      "on or after date" in {
        mustBeEqual("CreatedDate OnOrAfter 2000-01-01", "`t0`.`created_date` >= ?", List(DateTime.parse("2000-01-01")))
      }
      "before" in {
        mustBeEqual("CreatedDate Before NOW", "`t0`.`created_date` < NOW()", List.empty)
      }
      "more than 2 days from now" in {
        mustBeEqual("CreatedDate After 2d", "`t0`.`created_date` > DATE_ADD(NOW(), INTERVAL 2 DAY)", List.empty)
      }
      "after 12 months ago" in {
        mustBeEqual("CreatedDate After -12MONTHS", "`t0`.`created_date` > DATE_SUB(NOW(), INTERVAL 12 MONTH)", List.empty)
      }
    }

    "filter complex phrases" in {
      mustBeEqual("TableName = Person AND TableID > 2", "`t0`.`name` = ? AND `t0`.`id` > ?", List("Person", 2))
    }

  }
}
