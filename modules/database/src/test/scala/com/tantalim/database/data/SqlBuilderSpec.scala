package com.tantalim.database.data

import com.tantalim.models._
import org.junit.runner.RunWith
import org.specs2.mutable._
import org.specs2.runner._

@RunWith(classOf[JUnitRunner])
class SqlBuilderSpec extends Specification with FakeArtifacts {
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
      val sql = new SqlBuilder(table, Map(fakeModelFieldMap("TestPersonID", "id")))
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
      val sql = new SqlBuilder(table,
        fields = Map(fakeModelFieldMap("OtherName", "name", step = Some(step))),
        steps = Seq(step)
      )
      Util.stripExtraWhitespace(sql.toPreparedStatement) must be equalTo
        "SELECT `t1`.`name` AS `OtherName` FROM `test_table` AS `t0` LEFT JOIN `test_other` AS `t1` ON `t1`.`personID` = `t0`.`parentID`"
    }

    "optional then required step" in {
      val sql = {
        val personTable = DeepTable("Person", "person", module)

        val departmentStep = ModelStep(
          name = "OptionalDepartment",
          tableAlias = 1,
          join = TableJoin(
            name = "Department",
            table = ShallowTable("Department", "department", module),
            required = false,
            columns = Seq(TableJoinColumn(
              to = TableColumn("DepartmentID", "department_id"),
              from = Some(TableColumn("DepartmentID", "optional_department_id"))
            ))
          ),
          required = false,
          parentAlias = 0
        )
        val departmentChairStep: ModelStep = ModelStep(
          name = "RequiredDepartmentChair",
          tableAlias = 2,
          join = TableJoin(
            name = "ChairPerson",
            table = ShallowTable("Person", "person", module),
            required = true,
            columns = Seq(TableJoinColumn(
              to = TableColumn("PersonID", "person_id"),
              from = Some(TableColumn("ChairPerson", "chairperson_id"))
            ))
          ),
          required = true,
          parentAlias = departmentStep.tableAlias
        )
        new SqlBuilder(personTable,
          fields = Map(
            fakeModelFieldMap("PersonName", "name"),
            fakeModelFieldMap("DepartmentName", "name", step = Some(departmentStep)),
            fakeModelFieldMap("ChairPerson", "name", step = Some(departmentChairStep))
          ),
          steps = Seq(departmentStep, departmentChairStep)
        )
      }

      Util.stripExtraWhitespace(sql.toPreparedStatement) must be equalTo
        "SELECT `t0`.`name` AS `PersonName`, `t1`.`DepartmentName`, `t1`.`ChairPerson` FROM `person` AS `t0` " +
          "LEFT JOIN (SELECT `t0`.`department_id` AS `department_id`, `t0`.`name` AS `DepartmentName`, `t2`.`name` AS `ChairPerson` " +
          "FROM `department` AS `t0` INNER JOIN `person` AS `t2` ON `t2`.`person_id` = `t0`.`chairperson_id`" +
          ") AS `t1` ON `t1`.`department_id` = `t0`.`optional_department_id`"
    }

    "two required steps away" in {
      val sql = {
        val personTable = DeepTable("Person", "person", module)

        val departmentStep = ModelStep(
          name = "RequiredDepartment",
          tableAlias = 1,
          join = TableJoin(
            name = "Department",
            table = ShallowTable("Department", "department", module),
            required = true,
            columns = Seq(TableJoinColumn(
              to = TableColumn("DepartmentID", "department_id"),
              from = Some(TableColumn("DepartmentID", "required_department_id"))
            ))
          ),
          required = true,
          parentAlias = 0
        )
        val departmentChairStep: ModelStep = ModelStep(
          name = "RequiredDepartmentChair",
          tableAlias = 2,
          join = TableJoin(
            name = "ChairPerson",
            table = ShallowTable("Person", "person", module),
            required = true,
            columns = Seq(TableJoinColumn(
              to = TableColumn("PersonID", "person_id"),
              from = Some(TableColumn("ChairPerson", "chairperson_id"))
            ))
          ),
          required = true,
          parentAlias = departmentStep.tableAlias
        )
        new SqlBuilder(personTable,
          fields = Map(
            fakeModelFieldMap("PersonName", "name"),
            fakeModelFieldMap("DepartmentName", "name", step = Some(departmentStep)),
            fakeModelFieldMap("ChairPerson", "name", step = Some(departmentChairStep))
          ),
          steps = Seq(departmentStep, departmentChairStep)
        )
      }

      val result = "SELECT `t0`.`name` AS `PersonName`, `t1`.`name` AS `DepartmentName`, `t2`.`name` AS `ChairPerson` FROM `person` AS `t0` " +
        "INNER JOIN `department` AS `t1` ON `t1`.`department_id` = `t0`.`required_department_id` " +
        "INNER JOIN `person` AS `t2` ON `t2`.`person_id` = `t1`.`chairperson_id`"
      Util.stripExtraWhitespace(sql.toPreparedStatement) must be equalTo result
      // Make sure there is no "residual" effect from previous run
      Util.stripExtraWhitespace(sql.toPreparedStatement) must be equalTo result
    }
  }
}

object Util {
  def stripExtraWhitespace(value: String) = value.trim.replaceAll("\n", " ").replaceAll("  ", " ").replaceAll("  ", " ")
}