package com.tantalim.models

case class Module(name: String, database: Database)

object Module {
  def default = "default"
}
