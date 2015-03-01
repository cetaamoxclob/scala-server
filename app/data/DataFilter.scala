package data

import models.ModelField
import org.joda.time.DateTime
import com.tantalim.data.Comparator

object DataFilter {

  def parse(filter: String, fields: Map[String, ModelField]): (String, List[Any]) = {

    if (filter.isEmpty)
      return ("", List.empty)

    try {
      val patternAndOr = """(.+) (AND|OR) (.+)""".r
      val patternAndOr(leftPhrase, andOr, rightPhrase) = filter

      val (leftPhraseParsed, leftPhraseParams) = parse(leftPhrase, fields)
      val (rightPhraseParsed, rightPhraseParams) = parse(rightPhrase, fields)

      return (f"$leftPhraseParsed $andOr $rightPhraseParsed", leftPhraseParams ::: rightPhraseParams)
    } catch {
      case err: MatchError =>
    }

    try {
      val patternEquals = """(.+) (=|>|<|Equals|In|Contains|BeginsWith|EndsWith|Before|OnOrBefore|After|OnOrAfter|GreaterThan|GreaterThanOrEqual|LessThan|LessThanOrEqual|IsEmpty) (.+)""".r
      val patternEquals(left, comparatorCandidate, right) = filter

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
        return (f"${fieldToSql(fieldLeft.get)} ${comparatorToSql(comparator)} ${fieldToSql(fieldRight.get)}", List.empty)
      }

      val (rightSide, params): (String, List[Any]) = comparator match {
        case Comparator.IsEmpty => (f"IS NULL", List.empty)
        case Comparator.BeginsWith => (s"LIKE ?", List(right + "%"))
        case Comparator.EndsWith => (s"LIKE ?", List("%" + right))
        case Comparator.Contains => (s"LIKE ?", List("%" + right + "%"))
        case Comparator.In => {
          val valueListAsString = if (right.charAt(0) == '(' && right.last == ')') right.substring(1, right.length - 1)
          else right
          val values = valueListAsString.split(",").toList
          val bindings = values.map { value => "?"}.mkString(",")
          (f"IN ($bindings)", values.map(value => {
            value.replaceAll("\"", "").toInt
          }))
        }
        case _ => {
          if (fieldLeft.get.basisColumn.dataType == "Date") {
            val formattedDate = formatDate(right)
            if (formattedDate.isDefined)
              (comparatorToSql(comparator) + " " + formattedDate.get, List())
            else
              (comparatorToSql(comparator) + " ?", List(DateTime.parse(right)))
          } else {
            val parameter = if (fieldLeft.get.basisColumn.dataType == "Integer") {
              right.toInt
            } else right.toString
            (comparatorToSql(comparator) + " ?", List(parameter))
          }
        }
      }
      (fieldToSql(fieldLeft.get) + " " + rightSide, params)

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
