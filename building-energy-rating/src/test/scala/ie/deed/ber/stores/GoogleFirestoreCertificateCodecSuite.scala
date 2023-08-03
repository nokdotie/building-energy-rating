package ie.nok.ber.stores

import ie.nok.ber.{Certificate, given}
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
