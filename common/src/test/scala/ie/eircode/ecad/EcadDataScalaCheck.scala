package ie.eircode.ecad

import org.scalacheck.{Arbitrary, Gen}
import org.scalacheck.Arbitrary.arbitrary

implicit val genEcadDataFound: Gen[EcadData.Found] = for {
  eircode <- arbitrary[Eircode]
  geographicCoordinate <- arbitrary[GeographicCoordinate]
  geographicAddress <- arbitrary[GeographicAddress]
  postalAddress <- arbitrary[PostalAddress]
} yield EcadData.Found(
  eircode,
  geographicCoordinate,
  geographicAddress,
  postalAddress
)

implicit val genEcadDataNotFound: Gen[EcadData.NotFound.type] =
  Gen.const(EcadData.NotFound)

implicit val genEcadData: Gen[EcadData] =
  Gen.oneOf(
    genEcadDataFound,
    genEcadDataNotFound
  )

implicit val arbEcadData: Arbitrary[EcadData] = Arbitrary(
  genEcadData
)
