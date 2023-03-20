package ie.seai.ber.certificate

import org.scalacheck.{Arbitrary, Gen}
import org.scalacheck.Arbitrary.arbitrary
import java.time.{LocalDate, Year}
import ie.deed.ber.common.java.time.{arbLocalDate, arbYear}

implicit val genHtmlCertificate: Gen[HtmlCertificate] = for {
  rating <- arbitrary[Rating]
  typeOfRating <- arbitrary[TypeOfRating]
  issuedOn <- arbitrary[LocalDate]
  validUntil <- arbitrary[LocalDate]
  propertyAddress <- arbitrary[Address]
  propertyConstructedOn <- arbitrary[Year]
  propertyType <- arbitrary[PropertyType]
  propertyFloorArea <- arbitrary[SquareMeter]
  domesticEnergyAssessmentProcedureVersion <- arbitrary[
    DomesticEnergyAssessmentProcedureVersion
  ]
  energyRating <- arbitrary[KilowattHourPerSquareMetrePerYear]
  carbonDioxideEmissionsIndicator <- arbitrary[
    KilogramOfCarbonDioxidePerSquareMetrePerYear
  ]
} yield HtmlCertificate(
  rating,
  typeOfRating,
  issuedOn,
  validUntil,
  propertyAddress,
  propertyConstructedOn,
  propertyType,
  propertyFloorArea,
  domesticEnergyAssessmentProcedureVersion,
  energyRating,
  carbonDioxideEmissionsIndicator
)

implicit val arbHtmlCertificate: Arbitrary[HtmlCertificate] = Arbitrary(
  genHtmlCertificate
)
