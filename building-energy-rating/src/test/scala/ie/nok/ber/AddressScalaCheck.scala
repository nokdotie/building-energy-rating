package ie.nok.ber

import org.scalacheck.{Arbitrary, Gen}

private val genAddress: Gen[Address] =
  Gen.identifier.map { Address.fromString }

given Arbitrary[Address] = Arbitrary(genAddress)
