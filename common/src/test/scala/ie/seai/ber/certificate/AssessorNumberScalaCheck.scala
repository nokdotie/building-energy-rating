package ie.seai.ber.certificate

import org.scalacheck.{Arbitrary, Gen}

val genAssessorNumber: Gen[AssessorNumber] =
  Gen.posNum[Int].map { AssessorNumber.apply }

implicit val arbAssessorNumber: Arbitrary[AssessorNumber] =
  Arbitrary(genAssessorNumber)
