package data

import models.{ModelOrderBy, ModelStep, ModelField}

case class SqlBuilder(
                       from: String,
                       fields: Map[String, ModelField],
                       steps: Option[Map[Integer, ModelStep]] = None,
                       where: Option[String] = None,
                       orderBy: Seq[ModelOrderBy] = Seq.empty,
                       parameters: List[Any] = List.empty,
                       page: Int = 1,
                       limit: Int = 0) {

  def toPreparedStatement: String = {
    f"SELECT $getFields FROM `$from` AS `t0` $getJoins $getWhere $getOrderBy $getLimits"
  }

  private def getFields: String = {
    if (fields.isEmpty) "*"
    else fields.map {
      case (fieldName, f) => {
        s"`t0`.`${f.basisColumn.dbName}` AS `${fieldName}`"
      }
    }.toSeq.mkString(", ")
  }

  private def getJoins: String = {
    ""
  }

  private def getWhere: String = {
    if (where.isEmpty) ""
    else {
      "WHERE " + where.get
    }
  }

  private def getOrderBy: String = {
    if (orderBy.isEmpty) ""
    else "ORDER BY " + orderBy.map(modelOrderBy => {
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
