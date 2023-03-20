package ie.seai.ber.certificate

import org.scalacheck.{Arbitrary, Gen}

val genRating: Gen[Rating] =
  Gen.oneOf(Rating.values.toSeq)

implicit val arbRating: Arbitrary[Rating] =
  Arbitrary(genRating)
