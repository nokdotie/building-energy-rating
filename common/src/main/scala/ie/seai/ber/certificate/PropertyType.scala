package ie.seai.ber.certificate

import scala.util.{Try, Success, Failure}

enum PropertyType {
  case Maisonette
  case DetachedHouse, SemiDetachedHouse
  case GroundFloorApartment, MidFloorApartment, TopFloorApartment
  case MidTerraceHouse, EndOfTerraceHouse
}

object PropertyType {
  import PropertyType._

  def tryFromString(value: String): Try[PropertyType] = value match {
    case "Maisonette"             => Success(Maisonette)
    case "Detached house"         => Success(DetachedHouse)
    case "Semi-detached house"    => Success(SemiDetachedHouse)
    case "Mid-terrace house"      => Success(MidTerraceHouse)
    case "End of terrace house"   => Success(EndOfTerraceHouse)
    case "Ground-floor apartment" => Success(GroundFloorApartment)
    case "Mid-floor apartment"    => Success(MidFloorApartment)
    case "Top-floor apartment"    => Success(TopFloorApartment)
    case unknown => Failure(Exception(s"Unknown property type: $unknown"))
  }
}
