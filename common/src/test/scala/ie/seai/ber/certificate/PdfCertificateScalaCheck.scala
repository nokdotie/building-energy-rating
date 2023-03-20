package ie.seai.ber.certificate

import org.scalacheck.{Arbitrary, Gen}
import org.scalacheck.Arbitrary.arbitrary
import java.time.LocalDate
import ie.deed.ber.common.java.time.arbLocalDate

implicit val genPdfCertificate: Gen[PdfCertificate] = for {
  rating <- arbitrary[Rating]
  issuedOn <- arbitrary[LocalDate]
  validUntil <- arbitrary[LocalDate]
  propertyAddress <- arbitrary[Address]
  propertyEircode <- arbitrary[Option[Eircode]]
  assessorNumber <- arbitrary[AssessorNumber]
  assessorCompanyNumber <- arbitrary[AssessorCompanyNumber]
  domesticEnergyAssessmentProcedureVersion <- arbitrary[
    DomesticEnergyAssessmentProcedureVersion
  ]
  energyRating <- arbitrary[KilowattHourPerSquareMetrePerYear]
  carbonDioxideEmissionsIndicator <- arbitrary[
    KilogramOfCarbonDioxidePerSquareMetrePerYear
  ]
} yield PdfCertificate(
  rating,
  issuedOn,
  validUntil,
  propertyAddress,
  propertyEircode,
  assessorNumber,
  assessorCompanyNumber,
  domesticEnergyAssessmentProcedureVersion,
  energyRating,
  carbonDioxideEmissionsIndicator
)

implicit val arbPdfCertificate: Arbitrary[PdfCertificate] = Arbitrary(
  genPdfCertificate
)
