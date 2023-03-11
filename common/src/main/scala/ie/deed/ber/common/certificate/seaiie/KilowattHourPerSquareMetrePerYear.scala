package ie.deed.ber.common.certificate.seaiie

import scala.util.Try

final case class KilowattHourPerSquareMetrePerYear(value: Float) extends AnyVal
object KilowattHourPerSquareMetrePerYear {
    val seaiRegex = "^([ABC][123]|[DE][12]|[FG]) ([0-9.]+) \\(kWh/m2/yr\\)$".r
    def tryFromString(value: String): Try[KilowattHourPerSquareMetrePerYear] =
        seaiRegex.findFirstMatchIn(value)
            .flatMap { _.group(2).toFloatOption }
            .map { KilowattHourPerSquareMetrePerYear.apply }
            .toRight(Exception(s"Invalid kilowatt hour per square metre per year: $value"))
            .toTry
}
