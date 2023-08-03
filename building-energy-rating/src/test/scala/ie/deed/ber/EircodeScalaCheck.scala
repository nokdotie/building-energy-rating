package ie.nok.ber

import org.scalacheck.{Arbitrary, Gen}

private val genEircode: Gen[Eircode] =
  Gen.identifier.map { Eircode.apply }

given Arbitrary[Eircode] = Arbitrary(genEircode)
