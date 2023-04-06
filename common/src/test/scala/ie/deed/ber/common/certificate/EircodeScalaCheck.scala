package ie.deed.ber.common.certificate

import org.scalacheck.{Arbitrary, Gen}

import ie.deed.ber.common.certificate.Eircode
val genEircode: Gen[Eircode] =
  Gen.identifier.map { Eircode.apply }

implicit val arbEircode: Arbitrary[Eircode] =
  Arbitrary(genEircode)
