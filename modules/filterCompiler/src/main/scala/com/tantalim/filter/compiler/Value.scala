package com.tantalim.filter.compiler

import com.tantalim.data.Comparator
import com.tantalim.models.ModelField

case class Value(sql: Option[String] = None, values: List[Any] = List.empty) {
  def getInteger: Integer = 3

  def getField: Option[ModelField] = values.headOption.map(f => f.asInstanceOf[ModelField])

  def getComparator: Option[Comparator] = values.headOption.map(f => f.asInstanceOf[Comparator])

  def getString: String = values.headOption.map(f => String.valueOf(f)).getOrElse("")

}
