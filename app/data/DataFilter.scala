package data

import models.ModelField

import scala.util.matching.Regex

object DataFilter {

  def parse(filter: String, fields: Map[String, ModelField]): String = {
    if (filter.isEmpty)
      return ""

    try {
      val patternAndOr = """(.+) (AND|OR) (.+)""".r
      val patternAndOr(leftPhrase, andOr, rightPhrase) = filter
      println(s"parsing left `$leftPhrase` and right `$rightPhrase`")
      return f"${parse(leftPhrase, fields)} $andOr ${parse(rightPhrase, fields)}"
    } catch {
      case err: MatchError =>
    }

    try {
      val patternEquals = """(.+) (=|>|<|Equals|In|Contains|BeginsWith|EndsWith|Before|After|GreaterThan|GreaterThanOrEqual|LessThan|LessThanOrEqual) (.+)""".r
      val patternEquals(left, comparatorCandidate, right) = filter
      println(s"parsing single phrase left `$left` `$comparatorCandidate` right `$right`")

      val fieldLeft = fields.get(left)
      val fieldRight = fields.get(right)

      if (fieldLeft.isEmpty) {
        throw new Exception("SQL expressions must have a model field as the left side but received " + filter)
      }

      val comparator = {
        // workaround for "=" being a good option but not a very good Enum value
        if (comparatorCandidate == "=") Comparator.Equals
        else if (comparatorCandidate == ">") Comparator.GreaterThan
        else if (comparatorCandidate == "<") Comparator.LessThan
        else Comparator.valueOf(comparatorCandidate)
      }

      if (fieldRight.isDefined) {
        // We might want to consider warning if they choose and option that won't work here
        // For example dateField = stringField or field1 BeginsWith field2 or field1 = field1
        return f"${fieldToSql(fieldLeft.get)} ${comparatorToSql(comparator)} ${fieldToSql(fieldRight.get)}"
      }

      val rightSide = comparator match {
        case Comparator.IsEmpty => f"IS NULL"
        case Comparator.BeginsWith => s"LIKE '${right}%'"
        case Comparator.EndsWith => s"LIKE '%${right}'"
        case Comparator.Contains => s"LIKE '%${right}%'"
        case Comparator.In => f"IN ${right}"
        case _ => {
          if (fieldLeft.get.dataType == "Integer") {
            comparatorToSql(comparator) + " " + right
          } else if (fieldLeft.get.dataType == "Date") {
            comparatorToSql(comparator) + " " + formatDate(right)
          } else f"${comparatorToSql(comparator)} '${right}'"
        }
      }
      fieldToSql(fieldLeft.get) + " " + rightSide

    } catch {
      case err: MatchError => throw new Exception("Failed to match filter on " + filter)
    }
  }

  private def comparatorToSql(comparator: Comparator): String = {
    comparator match {
      case Comparator.Equals => "="
      case Comparator.In => "IN"
      case Comparator.GreaterThan | Comparator.After => ">"
      case Comparator.GreaterThanOrEquals | Comparator.OnOrAfter => ">="
      case Comparator.LessThan | Comparator.Before => "<"
      case Comparator.LessThanOrEquals | Comparator.OnOrBefore => "<="
      case Comparator.Contains | Comparator.BeginsWith | Comparator.EndsWith => "LIKE"
      case Comparator.IsEmpty => "IS NULL"
    }
  }

  private def fieldToSql(field: ModelField): String = {
    f"`t0`.`${field.dbName}`"
  }

  private def formatDate(dateMatcherCandidate: String): String = {
    val datePattern = dateMatcherCandidate.trim
    if (datePattern == "NOW") return "NOW()"

    try {

      val patternDateInterval = """(-?)(\d+)(\w+)""".r
      val patternDateInterval(negate, digit, date) = dateMatcherCandidate
      val dateAddFxn = if (negate == "-") "DATE_SUB" else "DATE_ADD"
      f"$dateAddFxn(NOW(), INTERVAL $digit ${toIntervalType(date)})"
    } catch {
      case err: MatchError => datePattern
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
