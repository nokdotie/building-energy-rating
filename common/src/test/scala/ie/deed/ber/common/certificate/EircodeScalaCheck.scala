package ie.nok.ber.common.certificate

import org.scalacheck.{Arbitrary, Gen}

import ie.nok.ber.common.certificate.Eircode
val genEircode: Gen[Eircode] =
  Gen.identifier.map { Eircode.apply }

implicit val arbEircode: Arbitrary[Eircode] =
  Arbitrary(genEircode)
