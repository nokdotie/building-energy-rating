package ie.seai.ber.certificate

import org.scalacheck.{Arbitrary, Gen}

val genAddress: Gen[Address] =
  Gen.identifier.map { Address.apply }

implicit val arbAddress: Arbitrary[Address] =
  Arbitrary(genAddress)
