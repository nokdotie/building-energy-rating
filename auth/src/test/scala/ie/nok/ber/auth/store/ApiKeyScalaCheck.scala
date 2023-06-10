package ie.nok.ber.auth.store

import org.scalacheck.{Arbitrary, Gen}
import ie.nok.ber.auth.model.ApiKeyType
import ie.nok.ber.auth.model.ApiKey

import java.time.Instant
import java.time.temporal.{ChronoUnit, TemporalUnit}
import java.util.Date

val genApiKeyType = Gen.oneOf(ApiKeyType.values.toSeq)

val genApiKey: Gen[ApiKey] = for {
  email <- Gen.asciiPrintableStr
  apiKey <- Gen.asciiPrintableStr
  apiKeyType <- genApiKeyType
  createdAt <- Gen.choose(
    Instant.now().minus(20, ChronoUnit.DAYS),
    Instant.now()
  )
} yield {
  ApiKey(
    email = email,
    apiKey = apiKey,
    apiKeyType = apiKeyType,
    createdAt = createdAt
  )
}

implicit val arbApiKey: Arbitrary[ApiKey] = Arbitrary(genApiKey)
