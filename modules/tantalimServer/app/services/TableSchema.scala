package services

import com.tantalim.models.Table

trait TableSchema {

  def generateTableDDL(table: Table): String = {
    val columnClause = table.columns.values.toList.sortWith(_.order < _.order).map { column =>
      val isPrimaryKey = table.primaryKey.isDefined && table.primaryKey.get.name == column.name

      val columnSql = new StringBuilder
      columnSql ++= "  `"
      columnSql ++= column.dbName
      columnSql ++= "` "
      columnSql ++= (column.dataType.toLowerCase match {
        case "boolean" => "TINYINT"
        case "date" => "DATE"
        case "datetime" => "DATETIME"
        case "int" | "integer" => "INT"
        case "Decimal" => "DECIMAL"
        case _ => "VARCHAR(50)" // TODO Include length
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


//`ClaimID` int(10) unsigned NOT NULL AUTO_INCREMENT,
//`PatientID` int(10) unsigned NOT NULL,
//`ClaimNumber` varchar(50) DEFAULT NULL,
//`ServiceDate` date DEFAULT NULL,
//`VendorID` int(11) DEFAULT NULL,
//`ProviderID` int(11) DEFAULT NULL,
//PRIMARY KEY (`ClaimID`)