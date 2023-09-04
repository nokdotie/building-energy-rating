package ie.nok.ber

import scala.util.chaining.scalaUtilChainingOps
import zio.json.{JsonCodec, DeriveJsonCodec}

case class Address(value: String)
object Address {
  def fromString(value: String): Address =
    value.trim
      .pipe { Address.apply }

  given JsonCodec[Address] = DeriveJsonCodec.gen[Address]
}
