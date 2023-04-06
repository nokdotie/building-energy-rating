package ie.deed.ber.common.certificate

import org.scalacheck.{Arbitrary, Gen}

import ie.deed.ber.common.certificate.AssessorNumber
val genAssessorNumber: Gen[AssessorNumber] =
  Gen.posNum[Int].map { AssessorNumber.apply }

implicit val arbAssessorNumber: Arbitrary[AssessorNumber] =
  Arbitrary(genAssessorNumber)
