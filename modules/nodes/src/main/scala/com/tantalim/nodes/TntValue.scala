package com.tantalim.nodes

import java.util.UUID
import org.joda.time.DateTime

sealed trait TntValue extends AnyRef {
  def rawString: String
}

case class TntNull() extends TntValue { def rawString = "NULL"}
case class TntString (var value: String) extends TntValue { def rawString = value}
case class TntDecimal (var value: BigDecimal) extends TntValue { def rawString = value.toString()}
case class TntInt (var value: BigInt) extends TntValue { def rawString = value.toString()}
case class TntBoolean (var value: Boolean) extends TntValue { def rawString = value.toString}
case class TntDate (var value: DateTime) extends TntValue { def rawString = value.toString}
case class TntTempID (var value: UUID = UUID.randomUUID) extends TntValue { def rawString = value.toString}
