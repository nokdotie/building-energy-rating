package ie.deed.ber.common.certificate

import org.scalacheck.{Arbitrary, Gen}
import org.scalacheck.Arbitrary.arbitrary
import ie.seai.ber.certificate._

implicit val genCertificate: Gen[Certificate] = for {
  number <- arbitrary[CertificateNumber]
  seaiIeHtmlCertificate <- arbitrary[Option[HtmlCertificate]]
  seaiIePdfCertificate <- arbitrary[Option[PdfCertificate]]
} yield Certificate(number, seaiIeHtmlCertificate, seaiIePdfCertificate)

implicit val arbCertificate: Arbitrary[Certificate] = Arbitrary(genCertificate)
