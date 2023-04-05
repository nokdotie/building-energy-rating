package ie.deed.ber.common.certificate

import org.scalacheck.{Arbitrary, Gen}
import org.scalacheck.Arbitrary.arbitrary
import ie.seai.ber.certificate._
import ie.eircode.ecad._

implicit val genCertificate: Gen[Certificate] = for {
  number <- arbitrary[CertificateNumber]
  seaiIePdfCertificate <- arbitrary[Option[PdfCertificate]]
  eircodeIeEcadData <- arbitrary[Option[EcadData]]
} yield Certificate(
  number,
  seaiIePdfCertificate,
  eircodeIeEcadData
)

implicit val arbCertificate: Arbitrary[Certificate] = Arbitrary(genCertificate)
