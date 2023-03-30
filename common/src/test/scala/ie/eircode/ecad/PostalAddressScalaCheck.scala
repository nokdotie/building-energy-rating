package ie.eircode.ecad

import org.scalacheck.{Arbitrary, Gen}

val genPostalAddress: Gen[PostalAddress] =
  Gen.identifier.map { PostalAddress.apply }

implicit val arbPostalAddress: Arbitrary[PostalAddress] =
  Arbitrary(genPostalAddress)
