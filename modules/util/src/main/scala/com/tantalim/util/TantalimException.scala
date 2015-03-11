package com.tantalim.util

class TantalimException(msg: String, help: String) extends Throwable(msg) {
  def getHelp = help
}
