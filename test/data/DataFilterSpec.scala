package data

import models.ModelField
import org.junit.runner.RunWith
import org.specs2.mutable._
import org.specs2.runner._

@RunWith(classOf[JUnitRunner])
class DataFilterSpec extends Specification {
  private def sampleField(name: String, dbName: String, dataType: String) = {
    name -> new ModelField(
      name,
      dbName,
      dataType,
      updateable = true,
      required = false
    )
  }

  val modelColumnSample: Map[String, ModelField] = Map(
    sampleField("TableID", "id", "Integer"),
    sampleField("CreatedDate", "created_date", "Date"),
    sampleField("TableName", "name", "String")
  )

  private def parseFilter(filter: String) = DataFilter.parse(filter, modelColumnSample)

  "DataFilter" should {
    "filter strings" in {
      "=" in {
        parseFilter("TableName = Person") must be equalTo "`t0`.`name` = 'Person'"
      }

      "Equals" in {
        parseFilter("TableName Equals Person") must be equalTo "`t0`.`name` = 'Person'"
      }

      "BeginsWith" in {
        parseFilter("TableName BeginsWith Person") must be equalTo "`t0`.`name` LIKE 'Person%'"
      }

      "EndsWith" in {
        parseFilter("TableName EndsWith Person") must be equalTo "`t0`.`name` LIKE '%Person'"
      }

      "Contains" in {
        parseFilter("TableName Contains Person") must be equalTo "`t0`.`name` LIKE '%Person%'"
      }
    }

    "filter numbers" in {
      "In" in {
        parseFilter("TableID In (1,2)") must be equalTo "`t0`.`id` IN (1,2)"
      }
    }

    "filter dates" in {
      "before" in {
        parseFilter("CreatedDate Before NOW") must be equalTo "`t0`.`created_date` < NOW()"
      }
      "more than 2 days from now" in {
        parseFilter("CreatedDate After 2d") must be equalTo "`t0`.`created_date` > DATE_ADD(NOW(), INTERVAL 2 DAY)"
      }
      "after 12 months ago" in {
        parseFilter("CreatedDate After -12MONTHS") must be equalTo "`t0`.`created_date` > DATE_SUB(NOW(), INTERVAL 12 MONTH)"
      }
    }

    "filter complex phrases" in {
      parseFilter("TableName = Person AND TableID > 2") must be equalTo "`t0`.`name` = 'Person' AND `t0`.`id` > 2"
    }

  }
}
