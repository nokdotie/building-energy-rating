package ie.seai.ber.certificate

import scala.util.{Try, Success, Failure}

enum PropertyType {
  case Maisonette, House
  case DetachedHouse, SemiDetachedHouse
  case Apartment, GroundFloorApartment, MidFloorApartment, TopFloorApartment
  case MidTerraceHouse, EndOfTerraceHouse
  case BasementDwelling
}

object PropertyType {
  import PropertyType._

  def tryFromString(value: String): Try[PropertyType] = value match {
    case "House"                  => Success(House)
    case "Maisonette"             => Success(Maisonette)
    case "Detached house"         => Success(DetachedHouse)
    case "Semi-detached house"    => Success(SemiDetachedHouse)
    case "Mid-terrace house"      => Success(MidTerraceHouse)
    case "End of terrace house"   => Success(EndOfTerraceHouse)
    case "Apartment"              => Success(Apartment)
    case "Ground-floor apartment" => Success(GroundFloorApartment)
    case "Mid-floor apartment"    => Success(MidFloorApartment)
    case "Top-floor apartment"    => Success(TopFloorApartment)
    case "Basement Dwelling"      => Success(BasementDwelling)
    case unknown => Failure(Exception(s"Unknown property type: $unknown"))
  }
}
