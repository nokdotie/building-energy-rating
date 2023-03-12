package ie.deed.ber.common.certificate.seaiie

import scala.util.{Try, Failure, Success}
import zio.json._

enum TypeOfRating {
  case NewDwelling
  case ExistingDwelling
}

object TypeOfRating {
  import TypeOfRating._

  def tryFromString(value: String): Try[TypeOfRating] = value match {
    case "New Dwelling"      => Success(NewDwelling)
    case "Existing Dwelling" => Success(ExistingDwelling)
    case unknown => Failure(Exception(s"Unknown type of rating: $unknown"))
  }

  implicit val encoder: JsonEncoder[TypeOfRating] =
    JsonEncoder[String].contramap(_.toString)
}
