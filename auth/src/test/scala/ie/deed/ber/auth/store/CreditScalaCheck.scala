package ie.deed.ber.auth.store

import ie.deed.ber.auth.model.Credit
import org.scalacheck.{Arbitrary, Gen}

import java.time.Instant

val genCredit: Gen[Credit] = for {
  email <- Gen.asciiPrintableStr
  number <- Gen.long
} yield {
  Credit(
    email = email,
    timestamp = Instant.now(),
    number = number
  )
}

implicit val arbCredit: Arbitrary[Credit] = Arbitrary(genCredit)
