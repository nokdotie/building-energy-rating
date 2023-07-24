package ie.deed.ber.auth.store

import ie.deed.ber.auth.model.Credit
import munit.{FunSuite, ScalaCheckSuite}
import org.scalacheck.Prop.forAll

import java.util

class GoogleFirestoreCreditCodecSuite extends ScalaCheckSuite {

  property("Credit encode and decode") {
    forAll { (credit: Credit) =>
      val encoded: util.Map[String, Any] =
        GoogleFirestoreCreditCodec.encode(credit)
      val decoded: Credit =
        GoogleFirestoreCreditCodec.decode(encoded)
      assertEquals(decoded, credit)
    }
  }
}
