package ie.deed.ber.common.certificate

import org.scalacheck.{Arbitrary, Gen}
import org.scalacheck.Arbitrary.arbitrary
import java.time.LocalDate

implicit val genCertificate: Gen[Certificate] = for {
  number <- arbitrary[CertificateNumber]
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
} yield Certificate(
  number,
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

implicit val arbCertificate: Arbitrary[Certificate] = Arbitrary(genCertificate)
