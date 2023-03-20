package ie.seai.ber.certificate

import org.scalacheck.{Arbitrary, Gen}

val genAssessorCompanyNumber: Gen[AssessorCompanyNumber] =
  Gen.posNum[Int].map { AssessorCompanyNumber.apply }

implicit val arbAssessorCompanyNumber: Arbitrary[AssessorCompanyNumber] =
  Arbitrary(genAssessorCompanyNumber)
