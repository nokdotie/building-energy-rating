package ie.nok.ber.common.certificate

import org.scalacheck.{Arbitrary, Gen}

import ie.nok.ber.common.certificate.AssessorNumber
val genAssessorNumber: Gen[AssessorNumber] =
  Gen.posNum[Int].map { AssessorNumber.apply }

implicit val arbAssessorNumber: Arbitrary[AssessorNumber] =
  Arbitrary(genAssessorNumber)
