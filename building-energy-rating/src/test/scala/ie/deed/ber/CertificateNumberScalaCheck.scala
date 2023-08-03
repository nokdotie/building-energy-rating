package ie.nok.ber

import org.scalacheck.{Arbitrary, Gen}
import org.scalacheck.Gen.chooseNum

private val genCertificateNumber: Gen[CertificateNumber] =
  chooseNum(
    CertificateNumber.MinValue.value,
    CertificateNumber.MaxValue.value
  ).map { CertificateNumber.apply }

given Arbitrary[CertificateNumber] = Arbitrary(genCertificateNumber)
