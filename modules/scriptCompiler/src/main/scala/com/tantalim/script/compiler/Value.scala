package com.tantalim.script.compiler

case class Value(value: Any = Unit) {

  def getString = value.asInstanceOf[String]

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
