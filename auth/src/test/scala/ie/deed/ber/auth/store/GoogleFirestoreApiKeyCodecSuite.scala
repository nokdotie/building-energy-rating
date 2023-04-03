package ie.deed.ber.auth.store

import com.google.cloud.Timestamp
import ie.deed.ber.auth.model.{ApiKey, ApiKeyType}
import munit.ScalaCheckSuite
import org.scalacheck.{Arbitrary, Gen}
import org.scalacheck.Prop.forAll

import java.time.Instant
import java.util
import java.util.Date

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
