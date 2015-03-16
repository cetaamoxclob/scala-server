package com.tantalim.script.compiler

case class Value(value: Any = Unit) {
  def asBoolean = value.asInstanceOf[Boolean]

  def isBoolean = value.isInstanceOf[Boolean]

  def asString = value.asInstanceOf[String]

  def isInt = value.isInstanceOf[Int]

  def asInt = value.asInstanceOf[Int]

  def isDouble = value.isInstanceOf[Double]

  def asDouble = if (isInt) asInt.toDouble else value.asInstanceOf[Double]

  def isNumeric = isInt || isDouble

  def toResult: Any = {
    value match {
      case valueValue: Value =>
        println(s"[WARN] Somehow value = $value was embedded in another Value object")
        valueValue.toResult
      case _ => value
    }
  }

  override def toString = value.toString
}
