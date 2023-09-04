package ie.nok.ber

import scala.util.chaining.scalaUtilChainingOps
import zio.json.{JsonCodec, DeriveJsonCodec}

case class Eircode(value: String)
object Eircode {
  def fromString(value: String): Eircode =
    value.toUpperCase
      .filter { _.isLetterOrDigit }
      .pipe { Eircode.apply }

  given JsonCodec[Eircode] = DeriveJsonCodec.gen[Eircode]
}
