package ie.nok.ber

import zio.json.{JsonCodec, DeriveJsonCodec}

case class KilowattHourPerSquareMetrePerYear(value: Float)

object KilowattHourPerSquareMetrePerYear {
  given JsonCodec[KilowattHourPerSquareMetrePerYear] =
    DeriveJsonCodec.gen[KilowattHourPerSquareMetrePerYear]
}
