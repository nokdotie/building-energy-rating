package ie.nok.ber.auth.store

import ie.nok.ber.auth.model.{ApiKey, ApiKeyType}
import ie.nok.ber.auth.store.GoogleFirestoreApiKeyCodec
import munit.ScalaCheckSuite
import org.scalacheck.{Arbitrary, Gen}
import org.scalacheck.Prop.forAll
import java.util

class GoogleFirestoreApiKeyCodecSuite extends ScalaCheckSuite {

  property("ApiKey encode and decode") {
    forAll { (apiKey: ApiKey) =>
      val encoded: util.Map[String, Any] =
        GoogleFirestoreApiKeyCodec.encode(apiKey)
      val decoded: ApiKey =
        GoogleFirestoreApiKeyCodec.decode(apiKey.apiKey, encoded)

      assertEquals(decoded, apiKey)
    }
  }
}
