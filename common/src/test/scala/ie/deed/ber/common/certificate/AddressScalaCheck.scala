package ie.deed.ber.common.certificate

import org.scalacheck.{Arbitrary, Gen}

import ie.deed.ber.common.certificate.Address
val genAddress: Gen[Address] =
  Gen.identifier.map { Address.apply }

implicit val arbAddress: Arbitrary[Address] =
  Arbitrary(genAddress)
