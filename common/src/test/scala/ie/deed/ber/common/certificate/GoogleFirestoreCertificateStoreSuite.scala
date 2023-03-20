package ie.deed.ber.common.certificate

import munit.ScalaCheckSuite
import org.scalacheck.Prop._

class GoogleFirestoreCertificateStoreSuite extends ScalaCheckSuite {
  property("certificate from and to a map") {
    forAll { (certificate: Certificate) =>
      val toMap = GoogleFirestoreCertificateStore.toMap(certificate)
      val fromMap =
        GoogleFirestoreCertificateStore.fromMap(certificate.number, toMap)

      assertEquals(certificate, fromMap)
    }
  }
}
