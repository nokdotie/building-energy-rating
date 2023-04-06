package ie.deed.ber.common.certificate

import org.scalacheck.{Arbitrary, Gen}

import ie.deed.ber.common.certificate.Rating
val genRating: Gen[Rating] =
  Gen.oneOf(Rating.values.toSeq)

implicit val arbRating: Arbitrary[Rating] =
  Arbitrary(genRating)
