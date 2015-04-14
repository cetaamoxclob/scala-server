package com.tantalim.database.data

import com.tantalim.models._
import org.junit.runner.RunWith
import org.specs2.mutable._
import org.specs2.runner._

@RunWith(classOf[JUnitRunner])
class SqlBuilderSpec extends Specification {
  "SqlBuilder" should {
    val module = Module("test-module", Database("TestDatabase"))
    val table = DeepTable("Test", "test_table", module)

    "getTableSql" in {
      val moduleWithDbName = Module("test-module", Database("TestDatabase", Some("test_database")))
      "Without dbName" in {
        SqlBuilder.getTableSql(DeepTable("Table", "test_table", moduleWithDbName)) must be equalTo "`test_database`.`test_table`"
      }
      "With dbName" in {
        SqlBuilder.getTableSql(DeepTable("Table", "test_table", module)) must be equalTo "`test_table`"
      }
    }

    "one table with no columns" in {
      val sql = new SqlBuilder(table)
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
      ))
      Util.stripExtraWhitespace(sql.toPreparedStatement) must be equalTo
        "SELECT `t0`.`id` AS `TestPersonID` FROM `test_table` AS `t0`"
    }

    "Where" in {
      val sql = new SqlBuilder(table, where = Some("PersonID = ?"))
      Util.stripExtraWhitespace(sql.toPreparedStatement) must be equalTo
        "SELECT * FROM `test_table` AS `t0` WHERE PersonID = ?"
    }

    "Order By" in {
      val sql = new SqlBuilder(table, orderBy = Seq(
        ModelOrderBy("PersonID")
      ))
      Util.stripExtraWhitespace(sql.toPreparedStatement) must be equalTo
        "SELECT * FROM `test_table` AS `t0` ORDER BY PersonID"
    }

    "Limits On Page 1" in {
      val sql = new SqlBuilder(table, limit = 100)
      Util.stripExtraWhitespace(sql.toPreparedStatement) must be equalTo
        "SELECT * FROM `test_table` AS `t0` LIMIT 100"
    }

    "Limits On Page 3" in {
      val sql = new SqlBuilder(table, limit = 100, page = 3)
      Util.stripExtraWhitespace(sql.toPreparedStatement) must be equalTo
        "SELECT * FROM `test_table` AS `t0` LIMIT 200, 100"
    }

    "LEFT JOIN" in {
      val joinTo = ShallowTable("Other", "test_other", module)

      val step = ModelStep(
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
      val field = ModelField("OtherName", TableColumn("Name", "name"), step = Some(step))
      val sql = new SqlBuilder(table, fields = Map(field.name -> field), steps = Map(step.tableAlias -> step))
      Util.stripExtraWhitespace(sql.toPreparedStatement) must be equalTo
        "SELECT `t1`.`name` AS `OtherName` FROM `test_table` AS `t0` LEFT JOIN `test_other` AS `t1` ON `t1`.`personID` = `t0`.`parentID`"
    }
  }
}

object Util {
  def stripExtraWhitespace(value: String) = value.trim.replaceAll("\n", " ").replaceAll("  ", " ").replaceAll("  ", " ")
}