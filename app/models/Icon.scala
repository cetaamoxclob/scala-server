package models

case class Icon(name: String) {
  def isFontAwesome = name.startsWith("fa-")

  def isGlyphicon = name.startsWith("glyphicon-")

  override def toString = name
}

