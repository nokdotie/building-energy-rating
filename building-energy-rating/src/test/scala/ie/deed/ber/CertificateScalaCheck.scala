package ie.nok.ber

import org.scalacheck.{Arbitrary, Gen}
import org.scalacheck.Arbitrary.arbitrary
import java.time.LocalDate

private val genCertificate: Gen[Certificate] = for {
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

given Arbitrary[Certificate] = Arbitrary(genCertificate)
