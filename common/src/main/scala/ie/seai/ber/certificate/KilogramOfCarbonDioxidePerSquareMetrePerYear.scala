package ie.seai.ber.certificate

import scala.util.Try

final case class KilogramOfCarbonDioxidePerSquareMetrePerYear(value: Float)
    extends AnyVal
object KilogramOfCarbonDioxidePerSquareMetrePerYear {
  val seaiIeHtmlRegex = "^([0-9.]+) \\(kgCO2/m2/yr\\)$".r
  val seaiIePdfRegex = "([0-9.]+) kgCO2 /m²/yr".r

  def tryFromString(
      value: String
  ): Try[KilogramOfCarbonDioxidePerSquareMetrePerYear] =
    seaiIeHtmlRegex
      .findFirstMatchIn(value)
      .orElse(seaiIePdfRegex.findFirstMatchIn(value))
      .flatMap(_.group(1).toFloatOption)
      .map { KilogramOfCarbonDioxidePerSquareMetrePerYear.apply }
      .toRight(
        Exception(
          s"Invalid kilogram of carbon dioxide per square metre per year: $value"
        )
      )
      .toTry
}
