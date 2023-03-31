package ie.eircode.ecad

import org.scalacheck.{Arbitrary, Gen}

val genGeographicAddress: Gen[GeographicAddress] =
  Gen.identifier.map { GeographicAddress.apply }

implicit val arbGeographicAddress: Arbitrary[GeographicAddress] =
  Arbitrary(genGeographicAddress)
