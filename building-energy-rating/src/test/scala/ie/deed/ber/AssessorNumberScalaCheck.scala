package ie.nok.ber

import org.scalacheck.{Arbitrary, Gen}

private val genAssessorNumber: Gen[AssessorNumber] =
  Gen.posNum[Int].map { AssessorNumber.apply }

given Arbitrary[AssessorNumber] = Arbitrary(genAssessorNumber)
