package ie.nok.ber

import java.time.{LocalDate, Year}
import zio.json.{JsonCodec, DeriveJsonCodec}

case class Certificate(
    number: CertificateNumber,
    rating: Rating,
    issuedOn: LocalDate,
    validUntil: LocalDate,
    propertyAddress: Address,
    propertyEircode: Option[Eircode],
    assessorNumber: AssessorNumber,
    assessorCompanyNumber: AssessorCompanyNumber,
    domesticEnergyAssessmentProcedureVersion: DomesticEnergyAssessmentProcedureVersion,
    energyRating: KilowattHourPerSquareMetrePerYear,
    carbonDioxideEmissionsIndicator: KilogramOfCarbonDioxidePerSquareMetrePerYear
)

object Certificate {
  given JsonCodec[Certificate] = DeriveJsonCodec.gen[Certificate]
}
