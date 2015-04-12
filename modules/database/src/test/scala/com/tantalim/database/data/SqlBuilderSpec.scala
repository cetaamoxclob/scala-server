package com.tantalim.database.data

import com.tantalim.models._
import org.junit.runner.RunWith
import org.specs2.mutable._
import org.specs2.runner._

@RunWith(classOf[JUnitRunner])
class SqlBuilderSpec extends Specification {
  "SqlBuilder" should {
    val module = Module("test-module", Database("test_database"))
    val table = DeepTable("Test", "test_table", module)

    "simple table" in {
      val sql = new SqlBuilder(table, Map.empty, Map.empty)
      "COUNT(*)" in {
        Util.stripExtraWhitespace(sql.toCalcRowsStatement) must be equalTo
          "SELECT COUNT(*) total FROM `test_table` AS `t0`"
      }

      "Simple FROM" in {
        Util.stripExtraWhitespace(sql.toPreparedStatement) must be equalTo
          "SELECT * FROM `test_table` AS `t0`"
      }
    }

    "Simple FROM" in {
      val sql = new SqlBuilder(table, Map(
        "TestPersonID" -> ModelField(
          "TestPersonID", TableColumn("PersonID", "id")
        )
      ), Map.empty)
      Util.stripExtraWhitespace(sql.toPreparedStatement) must be equalTo
        "SELECT `t0`.`id` AS `TestPersonID` FROM `test_table` AS `t0`"
    }

    "LEFT JOIN" in {
      val joinTo = ShallowTable("Other", "test_other", module)

      val sql = new SqlBuilder(table, Map.empty, Map(
        1 -> ModelStep(
          "TestStep",
          tableAlias = 1,
          join = TableJoin(
            "TestStep",
            joinTo,
            required = false,
            Seq(
              TableJoinColumn(
                to = TableColumn("PersonID", "personID"),
                from = Some(TableColumn("ParentID", "parentID")),
                fromText = None
              )
            )
          ),
          required = false,
          parentAlias = 0
        )
      ))
      Util.stripExtraWhitespace(sql.toPreparedStatement) must be equalTo
        "SELECT * FROM `test_table` AS `t0` LEFT JOIN `test_other` AS `t1` ON `t1`.`personID` = `t0`.`parentID`"
    }
  }
}

object Util {
  def stripExtraWhitespace(value: String) = value.trim.replaceAll("\n", " ").replaceAll("  ", " ")
}