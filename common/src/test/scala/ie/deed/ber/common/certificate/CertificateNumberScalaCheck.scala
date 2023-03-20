package ie.deed.ber.common.certificate

import org.scalacheck.{Arbitrary, Gen}
import org.scalacheck.Gen.chooseNum

val genCertificateNumber: Gen[CertificateNumber] =
  chooseNum(
    CertificateNumber.MinValue.value,
    CertificateNumber.MaxValue.value
  ).map { CertificateNumber.apply }

implicit val arbCertificateNumber: Arbitrary[CertificateNumber] =
  Arbitrary(genCertificateNumber)
