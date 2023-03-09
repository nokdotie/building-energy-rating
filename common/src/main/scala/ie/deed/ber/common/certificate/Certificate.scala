package ie.deed.ber.common.certificate

import java.time.{LocalDate, Year}
import scala.util.{Failure,Success}

final case class Certificate(
    number: CertificateNumber,
    typeOfRating: TypeOfRating,
    issuedOn: LocalDate,
    validUntil: LocalDate,

    propertyMeterPointReferenceNumber: Option[MeterPointReferenceNumber],
    propertyAddress: Address,
    propertyConstructedOn: Year,
    propertyType: PropertyType,
    propertyFloorArea: SquareMeter,

    domesticEnergyAssessmentProcedureVersion: DomesticEnergyAssessmentProcedureVersion,
    energyRating: KilowattHourPerSquareMetrePerYear,
    carbonDioxideEmissionsIndicator: KilogramOfCarbonDioxidePerSquareMetrePerYear,
)

final case class MeterPointReferenceNumber(value: Int) extends AnyVal
final case class Address(value: String) extends AnyVal

final case class SquareMeter(value: Float) extends AnyVal
object SquareMeter {
    val seaiRegex = "^([0-9.]+) \\(m2\\)$".r
    def tryFromString(value: String) =
        seaiRegex.findFirstMatchIn(value)
            .flatMap { _.group(1).toFloatOption }
            .map { SquareMeter.apply }
            .toRight(Exception(s"Invalid property floor area: $value"))
            .toTry
}

final case class KilowattHourPerSquareMetrePerYear(value: Float) extends AnyVal
object KilowattHourPerSquareMetrePerYear {
    val seaiRegex = "^([ABC][123]|[DE][12]|[FG]) ([0-9.]+) \\(kWh/m2/yr\\)$".r
    def tryFromString(value: String) =
        seaiRegex.findFirstMatchIn(value)
            .flatMap { _.group(2).toFloatOption }
            .map { KilowattHourPerSquareMetrePerYear.apply }
            .toRight(Exception(s"Invalid kilowatt hour per square metre per year: $value"))
            .toTry
}

final case class KilogramOfCarbonDioxidePerSquareMetrePerYear(value: Float) extends AnyVal
object KilogramOfCarbonDioxidePerSquareMetrePerYear {
    val seaiRegex = "^([0-9.]+) \\(kgCO2/m2/yr\\)$".r
    def tryFromString(value: String) =
        seaiRegex.findFirstMatchIn(value)
            .flatMap(_.group(1).toFloatOption)
            .map { KilogramOfCarbonDioxidePerSquareMetrePerYear.apply }
            .toRight(Exception(s"Invalid kilogram of carbon dioxide per square metre per year: $value"))
            .toTry
}

enum PropertyType {
    case DetachedHouse, SemiDetachedHouse
    case GroundFloorApartment, MidFloorApartment, TopFloorApartment
    case MidTerraceHouse, EndOfTerraceHouse
}

object PropertyType {
    import PropertyType._

    def tryFromString(value: String) = value match {
        case "Detached house" => Success(DetachedHouse)
        case "Semi-detached house" => Success(SemiDetachedHouse)
        case "Mid-terrace house" => Success(MidTerraceHouse)
        case "End of terrace house" => Success(EndOfTerraceHouse)
        case "Ground-floor apartment" => Success(GroundFloorApartment)
        case "Mid-floor apartment" => Success(MidFloorApartment)
        case "Top-floor apartment" => Success(TopFloorApartment)
        case unknown => Failure(Exception(s"Unknown property type: $unknown"))
    }
}

enum DomesticEnergyAssessmentProcedureVersion {
    case `3.2.1`
    case `4.0.0`, `4.1.0`
}

object DomesticEnergyAssessmentProcedureVersion {
    import DomesticEnergyAssessmentProcedureVersion._

    def tryFromString(value: String) = value match {
        case "3.2.1" => Success(`3.2.1`)
        case "4.0.0" => Success(`4.0.0`)
        case "4.1.0" => Success(`4.1.0`)
        case unknown => Failure(Exception(s"Unknown domestic energy assessment procedure version: $unknown"))
    }
}

enum TypeOfRating {
  case NewDwelling
  case ExistingDwelling
}

object TypeOfRating {
    import TypeOfRating._

    def tryFromString(value: String) = value match {
        case "New Dwelling" => Success(NewDwelling)
        case "Existing Dwelling" => Success(ExistingDwelling)
        case unknown => Failure(Exception(s"Unknown type of rating: $unknown"))
    }
}
