package ie.nok.ber.common.certificate

import org.scalacheck.{Arbitrary, Gen}

import ie.nok.ber.common.certificate.Address
val genAddress: Gen[Address] =
  Gen.identifier.map { Address.apply }

implicit val arbAddress: Arbitrary[Address] =
  Arbitrary(genAddress)
