package ie.deed.ber.common.certificate.seaiie

import scala.util.Try

final case class KilogramOfCarbonDioxidePerSquareMetrePerYear(value: Float)
    extends AnyVal
object KilogramOfCarbonDioxidePerSquareMetrePerYear {
  val seaiRegex = "^([0-9.]+) \\(kgCO2/m2/yr\\)$".r
  def tryFromString(
      value: String
  ): Try[KilogramOfCarbonDioxidePerSquareMetrePerYear] =
    seaiRegex
      .findFirstMatchIn(value)
      .flatMap(_.group(1).toFloatOption)
      .map { KilogramOfCarbonDioxidePerSquareMetrePerYear.apply }
      .toRight(
        Exception(
          s"Invalid kilogram of carbon dioxide per square metre per year: $value"
        )
      )
      .toTry
}
