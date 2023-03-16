package ie.seai.ber.certificate

import java.time.{LocalDate, Year}
import ie.seai.ber.certificate._

import ie.seai.ber.certificate.AssessorCompanyNumber
import ie.seai.ber.certificate.AssessorNumber
case class PdfCertificate(
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
