package ie.nok.ber

import org.scalacheck.{Arbitrary, Gen}

private val genAssessorCompanyNumber: Gen[AssessorCompanyNumber] =
  Gen.posNum[Int].map { AssessorCompanyNumber.apply }

given Arbitrary[AssessorCompanyNumber] = Arbitrary(genAssessorCompanyNumber)
