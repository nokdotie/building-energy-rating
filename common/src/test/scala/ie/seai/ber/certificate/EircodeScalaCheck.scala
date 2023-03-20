package ie.seai.ber.certificate

import org.scalacheck.{Arbitrary, Gen}

val genEircode: Gen[Eircode] =
  Gen.identifier.map { Eircode.apply }

implicit val arbEircode: Arbitrary[Eircode] =
  Arbitrary(genEircode)
