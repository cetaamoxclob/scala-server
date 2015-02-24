package data

import java.util.{Date, UUID}

sealed trait TntValue extends AnyRef

case class TntNull() extends TntValue
case class TntString (var value: String) extends TntValue
case class TntDecimal (var value: BigDecimal) extends TntValue
case class TntInt (var value: BigInt) extends TntValue
case class TntBoolean (var value: Boolean) extends TntValue
case class TntDate (var value: Date) extends TntValue
case class TntTempID (var value: UUID = UUID.randomUUID) extends TntValue
