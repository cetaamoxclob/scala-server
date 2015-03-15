package com.tantalim.script.compiler

case class Value(value: Any = Unit) {

  def getString = value.asInstanceOf[String]

  def toResult: Any = {
    value
  }

  override def toString = value.toString
}
