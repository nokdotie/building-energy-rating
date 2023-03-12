package ie.deed.ber.common.certificate.seaiie

import java.time.{LocalDate, Year}
import scala.util.{Failure, Success}
import zio.json._

final case class Certificate(
    typeOfRating: TypeOfRating,
    issuedOn: LocalDate,
    validUntil: LocalDate,
    propertyAddress: Address,
    propertyConstructedOn: Year,
    propertyType: PropertyType,
    propertyFloorArea: SquareMeter,
    domesticEnergyAssessmentProcedureVersion: DomesticEnergyAssessmentProcedureVersion,
    energyRating: KilowattHourPerSquareMetrePerYear,
    carbonDioxideEmissionsIndicator: KilogramOfCarbonDioxidePerSquareMetrePerYear
)

object Certificate {
  implicit val encoder: JsonEncoder[Certificate] =
    DeriveJsonEncoder.gen[Certificate]
}
