package ie.nok.ber.common.certificate

import org.scalacheck.{Arbitrary, Gen}

import ie.nok.ber.common.certificate.AssessorCompanyNumber
val genAssessorCompanyNumber: Gen[AssessorCompanyNumber] =
  Gen.posNum[Int].map { AssessorCompanyNumber.apply }

implicit val arbAssessorCompanyNumber: Arbitrary[AssessorCompanyNumber] =
  Arbitrary(genAssessorCompanyNumber)
