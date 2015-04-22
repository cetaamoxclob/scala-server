package com.tantalim.database.data

import com.tantalim.models._

case class SqlBuilder(
                       from: Table,
                       fields: Map[String, ModelField] = Map.empty,
                       steps: Seq[ModelStep] = Seq.empty,
                       where: Option[String] = None,
                       orderBy: Seq[ModelOrderBy] = Seq.empty,
                       parameters: List[Any] = List.empty,
                       page: Int = 1,
                       limit: Int = 0) {

  /**
   * tableAliasOverride is used in rare left join to inner join selects that require a derived query
   * and thus change the tableAlias
   */
  val tableAliasOverride: collection.mutable.Map[Int, Int] = collection.mutable.Map.empty

  def toPreparedStatement: String = {
    steps.foreach(step => println(s"Step ${step.name}(${step.join.table.dbName}) as ${step.tableAlias} required=${step.required} parent=${step.parentAlias}"))
    val joins = getJoins()
    f"SELECT $getFields \nFROM ${SqlBuilder.getTableSql(from)} AS `t0` $joins $getWhere $getOrderBy $getLimits"
  }

  /**
   * Calculate the total number of rows. We don't use SQL_CALC_FOUND_ROWS because
   * 1) SQL_CALC_FOUND_ROWS is the same speed as another COUNT(*) and we don't always need the page number
   * 2) SQL_CALC_FOUND_ROWS may not be supported in all RDMSs
   * @return
   */
  def toCalcRowsStatement: String = {
    // TODO we might be able to make this faster if getJoins only returns required joins
    f"SELECT COUNT(*) total FROM ${SqlBuilder.getTableSql(from)} AS `t0` ${getJoins()} $getWhere"
  }

  private def getFields: String = {
    if (fields.isEmpty) "*"
    else {
      fields.filter(f => f._2.basisColumn.isDefined).map {
        case (fieldName, f) =>
          if (f.step.isDefined && tableAliasOverride.get(f.step.get.tableAlias).isDefined) {
            val newAlias = tableAliasOverride.get(f.step.get.tableAlias).get
            s"`t$newAlias`.`$fieldName`"
          } else s"${getAlias(f)}.`${f.basisColumn.get.dbName}` AS `$fieldName`"
      }.toSeq.mkString(", ")
    }
  }

  private def getAlias(field: ModelField): String = {
    if (field.step.isEmpty) "`t0`"
    else s"`t${field.step.get.tableAlias}`"
  }

  private def getJoins(parentStep: Int = 0): String = {
    getChildSteps(parentStep).map { case (step) =>
      getJoinSql(step)
    }.toSeq.mkString("\n")
  }

  private def getJoinSql(step: ModelStep): String = {
    val requiredChildSteps = steps.filter(childStep => childStep.parentAlias == step.tableAlias && childStep.required)
    val from = "t" + step.parentAlias
    val to = "t" + step.tableAlias
    if (!step.required && requiredChildSteps.nonEmpty) {
      val columns = step.join.columns.map { joinColumn =>
        val fromVal = if (joinColumn.from.isDefined) s"`$from`.`${joinColumn.from.get.dbName}`"
        else s"'${joinColumn.fromText.get}'"
        s"`$to`.`${joinColumn.to.dbName}` = $fromVal"
      }.mkString(" AND ")

      val innerSql = {
        val extraFieldsInDerivedQuery: Map[String, ModelField] = step.join.columns.map { joinColumn =>
          val field: ModelField = ModelField(
            name = joinColumn.to.dbName,
            basisColumn = Some(joinColumn.to),
            dataType = joinColumn.to.dataType
          )
          field.name -> field
        }.toMap
        val descendentSteps: Seq[ModelStep] = getDescendentSteps(step.tableAlias).map { childStep =>
          if (childStep.parentAlias == step.tableAlias) childStep.copy(parentAlias = 0)
          else childStep
        }
        val descendentStepAliases = descendentSteps.map(dStep => dStep.tableAlias)
        val innerBuilder = SqlBuilder(
          from = step.join.table,
          fields = {
            val originalFieldsThatShouldBeInDerivedQuery: Map[String, ModelField] = this.fields.filter { field =>
              val fieldStep = field._2.step
              if (fieldStep.isEmpty) false
              else if (step == fieldStep.get) true
              else descendentStepAliases.contains(fieldStep.get.tableAlias)
            }

            val fieldsInDerivedQuery = originalFieldsThatShouldBeInDerivedQuery.map { field =>
              field._1 -> field._2.copy(
                step = if (field._2.step.get == step) None else field._2.step
              )
            }
            originalFieldsThatShouldBeInDerivedQuery.foreach(f => tableAliasOverride += (f._2.step.get.tableAlias -> step.tableAlias))

            extraFieldsInDerivedQuery ++ fieldsInDerivedQuery
          },
          steps = descendentSteps
        )
        innerBuilder.toPreparedStatement.trim
      }
      val columnClause = if (columns.isEmpty) "" else "ON " + columns

      s"LEFT JOIN ($innerSql) AS `$to` $columnClause"
    } else {
      val join = if (step.required) "INNER JOIN" else "LEFT JOIN"
      val columnClause = {
        val columns = step.join.columns.map { joinColumn =>
          val fromVal = if (joinColumn.from.isDefined) s"`$from`.`${joinColumn.from.get.dbName}`"
          else s"'${joinColumn.fromText.get}'"
          s"`$to`.`${joinColumn.to.dbName}` = $fromVal"
        }.mkString(" AND ")
        if (columns.isEmpty) ""
        else "ON " + columns
      }
      s"$join ${SqlBuilder.getTableSql(step.join.table)} AS `$to` $columnClause " +
        getChildSteps(step.tableAlias).map(getJoinSql).mkString("\n")
    }

  }

  private def getChildSteps(parentStep: Int): Seq[ModelStep] = {
    steps.filter(step => step.parentAlias == parentStep)
  }

  private def getDescendentSteps(parentStep: Int): Seq[ModelStep] = {
    val childSteps = steps.filter(step => step.parentAlias == parentStep)
    childSteps ++ childSteps.flatMap(childStep => getDescendentSteps(childStep.tableAlias))
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

object SqlBuilder {
  def getTableSql(table: Table): String = {
    val db = table.module.database.dbName
    if (db.isEmpty) s"`${table.dbName}`"
    else s"`${db.get}`.`${table.dbName}`"
  }
}

case class SqlField(name: String, sql: String)

case class SqlWhere(name: String, sql: String)
