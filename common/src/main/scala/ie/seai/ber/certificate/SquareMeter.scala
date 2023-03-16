package ie.seai.ber.certificate

import scala.util.Try

final case class SquareMeter(value: Float) extends AnyVal
object SquareMeter {
  val seaiRegex = "^([0-9.]+) \\(m2\\)$".r
  def tryFromString(value: String): Try[SquareMeter] =
    seaiRegex
      .findFirstMatchIn(value)
      .flatMap { _.group(1).toFloatOption }
      .map { SquareMeter.apply }
      .toRight(Exception(s"Invalid property floor area: $value"))
      .toTry
}
