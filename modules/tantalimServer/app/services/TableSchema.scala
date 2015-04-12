package services

import com.tantalim.database.data.{SqlBuilder, DatabaseConnection}
import com.tantalim.models.{DataType, Table}
import com.tantalim.util.TantalimException
import controllers.core.PlayableDatabaseConnection

trait TableSchema {
  val db = new DatabaseConnection with PlayableDatabaseConnection

  def drop(table: Table) = {
    checkMock(table)
    val sql = dropDDL(table)
    db.update(sql)
  }

  def create(table: Table) = {
    checkMock(table)
    val schemaSql = createDDL(table)
    db.update(schemaSql)
  }

  private def checkMock(table: Table): Unit = {
    if (table.isMock) {
      throw new TantalimException("Mock tables should not be created or deleted", "")
    }
  }

  def dropDDL(table: Table): String = {
    s"DROP TABLE IF EXISTS ${SqlBuilder.getTableSql(table)}"
  }

  def createDDL(table: Table): String = {
    val columnClause = table.columns.values.toList.sortWith(_.order < _.order).map { column =>
      val isPrimaryKey = table.primaryKey.isDefined && table.primaryKey.get.name == column.name

      val columnSql = new StringBuilder
      columnSql ++= "  `"
      columnSql ++= column.dbName
      columnSql ++= "` "
      columnSql ++= (column.dataType match {
        case DataType.Boolean => "TINYINT"
        case DataType.Date => "DATE"
        case DataType.DateTime => "DATETIME"
        case DataType.Integer => "INT"
        case DataType.Decimal => "DECIMAL"
        case DataType.String =>
          s"VARCHAR(${column.length.getOrElse(50)})"
      })
      columnSql ++= (if (column.required) " NOT NULL" else " NULL")
      if (isPrimaryKey) {
        columnSql ++= " AUTO_INCREMENT PRIMARY KEY"
      }
      columnSql.toString()
    }.toSeq.mkString(",\n")
    s"""CREATE TABLE ${SqlBuilder.getTableSql(table)} (
       |$columnClause
       |) ENGINE=InnoDB DEFAULT CHARSET=utf8;""".stripMargin
  }

}
