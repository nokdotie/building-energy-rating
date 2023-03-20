package ie.seai.ber.certificate

import org.scalacheck.{Arbitrary, Gen}

val genSquareMeter: Gen[SquareMeter] =
  Gen.posNum[Float].map { SquareMeter.apply }

implicit val arbSquareMeter: Arbitrary[SquareMeter] =
  Arbitrary(genSquareMeter)
