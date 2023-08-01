package ie.nok.ber.common.certificate.stores

import ie.nok.ber.common.certificate._
import munit.ScalaCheckSuite
import org.scalacheck.Prop._

class GoogleFirestoreCertificateCodecSuite extends ScalaCheckSuite {
  property("certificate encode and decode") {
    forAll { (certificate: Certificate) =>
      val encoded = GoogleFirestoreCertificateCodec.encode(certificate)
      val decoded = GoogleFirestoreCertificateCodec.decode(encoded)

      assertEquals(decoded, certificate)
    }
  }
}
