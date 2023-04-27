package ie.deed.ber.auth.store

import ie.deed.ber.auth.model.{ApiKey, UserRequest}
import org.scalacheck.{Arbitrary, Gen}

import java.time.Instant
import java.time.temporal.ChronoUnit

val genUserRequest: Gen[UserRequest] = for {
  email <- Gen.asciiPrintableStr
  request <- Gen.asciiPrintableStr
} yield {
  UserRequest(
    email = email,
    timestamp = Instant.now(),
    request = request
  )
}

implicit val arbUserRequest: Arbitrary[UserRequest] = Arbitrary(genUserRequest)
