package ie.nok.ber

import zio.json.{JsonCodec, DeriveJsonCodec}

case class KilogramOfCarbonDioxidePerSquareMetrePerYear(value: Float)

object KilogramOfCarbonDioxidePerSquareMetrePerYear {
  given JsonCodec[KilogramOfCarbonDioxidePerSquareMetrePerYear] =
    DeriveJsonCodec.gen[KilogramOfCarbonDioxidePerSquareMetrePerYear]
}
