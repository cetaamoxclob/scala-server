package com.tantalim.filter.compiler

import com.tantalim.filter.compiler.src._
import com.tantalim.models.{DataType, ModelField}
import com.tantalim.data.Comparator
import org.antlr.v4.runtime.{CommonTokenStream, ANTLRInputStream}

class CompileFilter(filter: String, fields: Map[String, ModelField]) extends FilterBaseVisitor[CompiledFilter] {

  private def parser = new FilterParser(
    new CommonTokenStream(
      new FilterLexer(
        new ANTLRInputStream(filter)
      )))

  def parse() = {
    visit(parser.start)
  }

  override def visitOperatorExpr(ctx: FilterParser.OperatorExprContext) = {
    val left = ctx.left.getText
    val right = ctx.right.getText

    val fieldLeft = fields.get(left)
    val fieldRight = fields.get(right)

    if (fieldLeft.isEmpty) {
      throw new Exception("SQL expressions must have a model field as the left side but received " + filter)
    }

    val comparator = {
      val comparatorCandidate: String = ctx.op.getText
      // workaround for "=" being a good option but not a very good Enum value
      if (comparatorCandidate == "=") Comparator.Equals
      else if (comparatorCandidate == ">") Comparator.GreaterThan
      else if (comparatorCandidate == "<") Comparator.LessThan
      else Comparator.valueOf(comparatorCandidate)
    }
    val (rightSide, params): (String, List[Any]) = comparator match {
      case Comparator.IsEmpty => (f"IS NULL", List.empty)
      case Comparator.BeginsWith => (s"LIKE ?", List(right + "%"))
      case Comparator.EndsWith => (s"LIKE ?", List("%" + right))
      case Comparator.Contains => (s"LIKE ?", List("%" + right + "%"))
      case Comparator.In =>
        val valueListAsString = if (right.charAt(0) == '(' && right.last == ')') right.substring(1, right.length - 1)
        else right
        val values = valueListAsString.split(",").toList
        val bindings = values.map { value => "?"}.mkString(",")
        (f"IN ($bindings)", values.map(value => {
          value.replaceAll("\"", "").toInt
        }))
      case _ =>
        fieldLeft.get.basisColumn.dataType match {
          case DataType.Date | DataType.DateTime =>
            val formattedDate = CompileFilter.formatDate(right)
            if (formattedDate.isDefined)
              (CompileFilter.comparatorToSql(comparator) + " " + formattedDate.get, List())
            else {
              import org.joda.time.DateTime
              (CompileFilter.comparatorToSql(comparator) + " ?", List(DateTime.parse(right)))
            }
          case DataType.Integer =>
            (CompileFilter.comparatorToSql(comparator) + " ?", List(right.toInt))
          case DataType.Decimal =>
            (CompileFilter.comparatorToSql(comparator) + " ?", List(right.toFloat))
          case DataType.Boolean =>
            (CompileFilter.comparatorToSql(comparator) + " ?", List(right.toBoolean))
          case _ =>
            (CompileFilter.comparatorToSql(comparator) + " ?", List(right.toString))
        }
    }
    CompiledFilter(CompileFilter.fieldToSql(fieldLeft.get) + " " + rightSide, params)
  }

  override def visitStart(ctx: FilterParser.StartContext) = {
    visit(ctx.expr())
  }

  override def visitParenthesisExpr(ctx: FilterParser.ParenthesisExprContext) = {
    val content = visit(ctx.expr())
    content.copy(sql = s"(${content.sql})")
  }

}

object CompileFilter {
  private def comparatorToSql(comparator: Comparator): String = {
    comparator match {
      case Comparator.Equals => "="
      case Comparator.NotEquals => "<>"
      case Comparator.In => "IN"
      case Comparator.NotIn => "NOT IN"
      case Comparator.GreaterThan | Comparator.After => ">"
      case Comparator.GreaterThanOrEqual | Comparator.OnOrAfter => ">="
      case Comparator.LessThan | Comparator.Before => "<"
      case Comparator.LessThanOrEqual | Comparator.OnOrBefore => "<="
      case Comparator.Contains | Comparator.BeginsWith | Comparator.EndsWith => "LIKE"
      case Comparator.IsEmpty => "IS NULL"
    }
  }

  private def fieldToSql(field: ModelField): String = {
    f"`t0`.`${field.basisColumn.dbName}`"
  }

  private def formatDate(dateMatcherCandidate: String): Option[String] = {
    val datePattern = dateMatcherCandidate.trim
    if (datePattern == "NOW") return Some("NOW()")

    try {
      val patternDateInterval = """(-?)(\d+)(\w+)""".r
      val patternDateInterval(negate, digit, date) = dateMatcherCandidate
      val dateAddFxn = if (negate == "-") "DATE_SUB" else "DATE_ADD"
      Some(f"$dateAddFxn(NOW(), INTERVAL $digit ${toIntervalType(date)})")
    } catch {
      case err: MatchError => None
    }
  }

  private def toIntervalType(interval: String): String = {
    interval.toUpperCase match {
      case "D" | "DAY" | "DAYS" => "DAY"
      case "W" | "WEEK" | "WEEKS" => "WEEK"
      case "M" | "MONTH" | "MONTHS" => "MONTH"
      case "Y" | "YEAR" | "YEARS" => "YEAR"
      case _ => throw new Exception("Invalid interval type: " + interval)
    }
  }

}