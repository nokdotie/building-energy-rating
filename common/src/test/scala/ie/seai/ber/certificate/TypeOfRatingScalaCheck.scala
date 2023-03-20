package ie.seai.ber.certificate

import org.scalacheck.{Arbitrary, Gen}

val genTypeOfRating: Gen[TypeOfRating] =
  Gen.oneOf(TypeOfRating.values.toSeq)

implicit val arbTypeOfRating: Arbitrary[TypeOfRating] =
  Arbitrary(genTypeOfRating)
