package com.tantalim.filter.compiler

import com.tantalim.filter.compiler.src.FilterParser._
import com.tantalim.filter.compiler.src._
import com.tantalim.models.{DataType, ModelField}
import com.tantalim.data.Comparator
import org.antlr.v4.runtime.{CommonTokenStream, ANTLRInputStream}

class CompileFilter(filter: String, fields: Map[String, ModelField]) extends FilterBaseVisitor[Value] {

  private def parser = new FilterParser(
    new CommonTokenStream(
      new FilterLexer(
        new ANTLRInputStream(filter)
      )))

  def parse() = {
    val value = visit(parser.start)
    CompiledFilter(value.sql.get, value.values)
  }

  override def visitStart(ctx: FilterParser.StartContext) = {
    visit(ctx.phrase())
  }

  override def visitAndPhrase(ctx: FilterParser.AndPhraseContext) = {
    val left = visit(ctx.left)
    val right = visit(ctx.right)
    val andOr = ctx.andor.getText.toUpperCase
    Value(Some(s"${left.sql} $andOr ${right.sql}"), left.values ++: right.values)
  }

  override def visitParenthesisPhrase(ctx: FilterParser.ParenthesisPhraseContext) = {
    val content = visit(ctx.phrase())
    content.copy(sql = Some(s"(${content.sql.getOrElse("")})"))
  }

  override def visitStatementPhrase(ctx: FilterParser.StatementPhraseContext) = {
    val leftField = visit(ctx.left)
    val comparator = visit(ctx.comparator)
    val right = visit(ctx.right)

    val (rightSide, params) = calculateRightSideWithParams(leftField.getField.get.basisColumn.dataType, comparator, right)
    Value(sql = Some(leftField.sql.get + " " + comparator.sql.get + " " + rightSide), params)
  }

  override def visitField(ctx: FilterParser.FieldContext) = {
    val fieldName = ctx.getText
    val field = fields.getOrElse(fieldName,
      throw new Exception("SQL expressions must have a model field as the left side but received " + fieldName)
    )
    println("Found field " + field.name)
    Value(Some(s"`t0`.`${field.basisColumn.dbName}`"), List(field))
  }

  override def visitComparators(ctx: FilterParser.ComparatorsContext) = {
    val comparator = {
      val comparatorCandidate = ctx.getText
      if (comparatorCandidate == "=") Comparator.Equals
      else if (comparatorCandidate == ">") Comparator.GreaterThan
      else if (comparatorCandidate == "<") Comparator.LessThan
      else Comparator.valueOf(comparatorCandidate)
    }
    val sql = comparator match {
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

    Value(Some(sql), List(comparator))
  }

  override def visitStringAtom(ctx: FilterParser.StringAtomContext) = {
    val raw = ctx.getText
    val cleanedUp = raw.substring(1, raw.length() - 1).replace("\'", "'").replace("\\\"", "\"")
    Value(values = List(cleanedUp))
  }

  override def visitNumberAtom(ctx: FilterParser.NumberAtomContext) = {
    val intValue = ctx.getText.toInt
    println("intValue = " + intValue)
    Value(values = List(intValue))
  }

  override def visitListAtom(ctx: FilterParser.ListAtomContext) = {
    import scala.collection.JavaConverters._
    val itemList = ctx.basicAtom.asScala
    Value(values = itemList.flatMap(a => visit(a).values).toList)
  }

  private def calculateRightSideWithParams(leftDataType: DataType, comparator: Value, rightContext: Value): (String, List[Any]) = {
    val right = rightContext
//    val fieldRight = fields.get(right)

    comparator.getComparator.get match {
      case Comparator.IsEmpty => ("", List.empty)
      case Comparator.BeginsWith => ("?", List(right.getString + "%"))
      case Comparator.EndsWith => ("?", List("%" + right.getString))
      case Comparator.Contains => ("?", List("%" + right.getString + "%"))
      case Comparator.In =>
        ("(" + rightContext.values.map(_ => "?").mkString(",") + ")", rightContext.values)
      case _ =>
        leftDataType match {
//          case DataType.Date | DataType.DateTime =>
//            val formattedDate = CompileFilter.formatDate(right)
//            if (formattedDate.isDefined)
//              ("?" + formattedDate.get, List())
//            else {
//              import org.joda.time.DateTime
//              ("?", List(DateTime.parse(right)))
//            }
          case DataType.Integer =>
            ("?", List(right.getInteger))
//          case DataType.Decimal =>
//            ("?", List(right.toFloat))
//          case DataType.Boolean =>
//            ("?", List(right.toBoolean))
          case _ =>
            ("?", List(rightContext.getString))
        }
    }
  }
}

object CompileFilter {
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