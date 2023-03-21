package ie.seai.ber.certificate

import java.time.{LocalDate, Year}
import ie.deed.ber.common.certificate.CertificateNumber

case class PdfCertificate(
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

object PdfCertificate {
  def url(certificateNumber: CertificateNumber): String =
    s"https://ndber.seai.ie/PASS/Download/PassDownloadBER.ashx?type=nas&file=bercert&ber=${certificateNumber.value}"
}
