package ie.seai.ber.certificate

import scala.util.Try

final case class KilowattHourPerSquareMetrePerYear(value: Float) extends AnyVal
object KilowattHourPerSquareMetrePerYear {
  val seaiIeHtmlRegex = "([0-9.]+) \\(kWh/m2/yr\\)".r
  val seaiIePdfRegex = "([0-9.]+) kWh/m²/yr".r

  def tryFromString(value: String): Try[KilowattHourPerSquareMetrePerYear] =
    seaiIeHtmlRegex
      .findFirstMatchIn(value)
      .orElse(seaiIePdfRegex.findFirstMatchIn(value))
      .flatMap { _.group(1).toFloatOption }
      .map { KilowattHourPerSquareMetrePerYear.apply }
      .toRight(
        Exception(s"Invalid kilowatt hour per square metre per year: $value")
      )
      .toTry
}
