package data

import com.tantalim.models.DataType

object ComparatorByDataType {
  def get(dataType: DataType): Seq[Comparator] = {
    val defaults = Seq(
      Comparator.Equals,
      Comparator.NotEquals,
      Comparator.IsEmpty
    )
    dataType match {
      case DataType.Boolean => defaults
      case DataType.Date | DataType.DateTime => defaults ++: Seq(
        Comparator.Before,
        Comparator.After,
        Comparator.OnOrBefore,
        Comparator.OnOrAfter
      )
      case DataType.Integer | DataType.Decimal => defaults ++: Seq(
        Comparator.GreaterThan,
        Comparator.GreaterThanOrEqual,
        Comparator.LessThan,
        Comparator.LessThanOrEqual
      )
      case DataType.String => defaults ++: Seq(
        Comparator.Contains
      )
    }
  }

}
