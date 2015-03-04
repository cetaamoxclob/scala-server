package services

import com.tantalim.models.{DataType, Table}

trait TableSchema {

  def generateTableDDL(table: Table): String = {
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
        case DataType.String => "VARCHAR(50)"
      })
      columnSql ++= (if (column.required) " NOT NULL" else " NULL")
      if (isPrimaryKey) {
        columnSql ++= " AUTO_INCREMENT PRIMARY KEY"
      }
      columnSql.toString()
    }.toSeq.mkString(",\n")
    s"""DROP TABLE IF EXISTS `${table.dbName}`;
       |
       |CREATE TABLE `${table.dbName}` (
       |$columnClause
       |) ENGINE=InnoDB DEFAULT CHARSET=utf8;""".stripMargin
  }

}
