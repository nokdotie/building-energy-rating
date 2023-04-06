package ie.deed.ber.common.certificate

import java.time.{LocalDate, Year}

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
