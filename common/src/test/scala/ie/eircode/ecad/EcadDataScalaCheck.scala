package ie.eircode.ecad

import org.scalacheck.{Arbitrary, Gen}
import org.scalacheck.Arbitrary.arbitrary

implicit val genEcadData: Gen[EcadData] = for {
  eircode <- arbitrary[Eircode]
  geographicCoordinate <- arbitrary[GeographicCoordinate]
  geographicAddress <- arbitrary[GeographicAddress]
  postalAddress <- arbitrary[PostalAddress]
} yield EcadData(
  eircode,
  geographicCoordinate,
  geographicAddress,
  postalAddress
)

implicit val arbEcadData: Arbitrary[EcadData] = Arbitrary(
  genEcadData
)
