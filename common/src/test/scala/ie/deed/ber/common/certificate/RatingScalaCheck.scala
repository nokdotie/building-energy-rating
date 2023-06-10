package ie.nok.ber.common.certificate

import org.scalacheck.{Arbitrary, Gen}

import ie.nok.ber.common.certificate.Rating
val genRating: Gen[Rating] =
  Gen.oneOf(Rating.values.toSeq)

implicit val arbRating: Arbitrary[Rating] =
  Arbitrary(genRating)
