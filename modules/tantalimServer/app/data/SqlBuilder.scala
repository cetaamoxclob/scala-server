package data

import com.tantalim.models.{ModelOrderBy, ModelStep, ModelField}

case class SqlBuilder(
                       from: String,
                       fields: Map[String, ModelField],
                       steps: scala.collection.Map[Int, ModelStep],
                       where: Option[String] = None,
                       orderBy: Seq[ModelOrderBy] = Seq.empty,
                       parameters: List[Any] = List.empty,
                       page: Int = 1,
                       limit: Int = 0) {

  def toPreparedStatement: String = {
    f"SELECT $getFields \nFROM `$from` AS `t0` $getJoins $getWhere $getOrderBy $getLimits"
  }

  /**
   * Calculate the total number of rows. We don't use SQL_CALC_FOUND_ROWS because
   * 1) SQL_CALC_FOUND_ROWS is the same speed as another COUNT(*) and we don't always need the page number
   * 2) SQL_CALC_FOUND_ROWS may not be supported in all RDMSs
   * @return
   */
  def toCalcRowsStatement: String = {
    // TODO we might be able to make this faster if getJoins only returns required joins
    f"SELECT COUNT(*) total FROM `$from` AS `t0` $getJoins $getWhere"
  }

  private def getFields: String = {
    if (fields.isEmpty) "*"
    else fields.map {
      case (fieldName, f) =>
        s"${getAlias(f)}.`${f.basisColumn.dbName}` AS `$fieldName`"
    }.toSeq.mkString(", ")
  }

  private def getAlias(field: ModelField): String = {
    if (field.step.isEmpty) "`t0`"
    else s"`t${field.step.get.tableAlias}`"
  }

  private def getJoins: String = {
    steps.map { case (alias, step) =>
      val join = if (step.required) "INNER JOIN" else "LEFT JOIN"
      val from = "t" + step.parentAlias
      val to = "t" + step.tableAlias
      val columns = step.join.columns.map{ joinColumn =>
        val fromVal = if (joinColumn.from.isDefined) s"`$from`.`${joinColumn.from.get.dbName}`"
        else s"'${joinColumn.fromText.get}'"
        s"`$to`.`${joinColumn.to.dbName}` = $fromVal"
      }.mkString(" AND ")
      val columnClause = if (columns.isEmpty) ""
      else "ON " + columns
      s"$join `${step.join.table.dbName}` AS `$to` $columnClause"
    }.toSeq.mkString("\n")
  }

  private def getWhere: String = {
    if (where.isEmpty) ""
    else {
      "\nWHERE " + where.get
    }
  }

  private def getOrderBy: String = {
    if (orderBy.isEmpty) ""
    else "\nORDER BY " + orderBy.map(modelOrderBy => {
      val ascDesc = if (modelOrderBy.ascending.isDefined && !modelOrderBy.ascending.get) " DESC" else ""
      modelOrderBy.fieldName + ascDesc
    }).mkString(", ")
  }

  private def getLimits: String = {
    if (limit > 0) {
      if (page > 1) {
        val start = (page - 1) * limit
        s"LIMIT $start, $limit"
      }
      else s"LIMIT $limit"
    } else ""
  }
}

case class SqlField(name: String, sql: String)

case class SqlWhere(name: String, sql: String)
