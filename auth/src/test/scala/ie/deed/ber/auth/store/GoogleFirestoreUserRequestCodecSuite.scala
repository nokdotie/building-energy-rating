package ie.deed.ber.auth.store

import ie.deed.ber.auth.model.UserRequest
import munit.{FunSuite, ScalaCheckSuite}
import org.scalacheck.Prop.forAll
import java.util

class GoogleFirestoreUserRequestCodecSuite extends ScalaCheckSuite {

  property("UserRequest encode and decode") {
    forAll { (userRequest: UserRequest) =>
      val encoded: util.Map[String, Any] =
        GoogleFirestoreUserRequestCodec.encode(userRequest)
      val decoded: UserRequest = GoogleFirestoreUserRequestCodec.decode(encoded)
      assertEquals(decoded, userRequest)
    }
  }
}
