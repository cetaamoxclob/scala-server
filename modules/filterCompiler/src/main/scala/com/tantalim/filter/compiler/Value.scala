package com.tantalim.filter.compiler

import com.tantalim.data.Comparator
import com.tantalim.models.ModelField

case class Value(sql: Option[String] = None, values: List[Any] = List.empty) {

  def getBoolean: Boolean = values.head.asInstanceOf[Boolean]

  def getInteger: Integer = values.head match {
    case v: Int => v
    case _ => values.head.toString.toInt
  }

  def getFloat: Float = values.head.asInstanceOf[Float]

  def getField: Option[ModelField] = values.headOption.map(f => f.asInstanceOf[ModelField])

  def getComparator: Option[Comparator] = values.headOption.map(f => f.asInstanceOf[Comparator])

  def getString: String = values.headOption.map(f => String.valueOf(f)).getOrElse("")

}
