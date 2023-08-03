package ie.nok.ber

import org.scalacheck.{Arbitrary, Gen}

private val genRating: Gen[Rating] =
  Gen.oneOf(Rating.values.toSeq)

given Arbitrary[Rating] = Arbitrary(genRating)
