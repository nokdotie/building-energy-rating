package ie.deed.ber.common.certificate.stores

import ie.deed.ber.common.certificate._
import munit.ScalaCheckSuite
import org.scalacheck.Prop._

class GoogleFirestoreCertificateCodecSuite extends ScalaCheckSuite {
  property("certificate encode and decode") {
    forAll { (certificate: Certificate) =>
      val encoded = GoogleFirestoreCertificateCodec.encode(certificate)
      val decoded =
        GoogleFirestoreCertificateCodec.decode(certificate.number, encoded)

      assertEquals(decoded, certificate)
    }
  }
}
