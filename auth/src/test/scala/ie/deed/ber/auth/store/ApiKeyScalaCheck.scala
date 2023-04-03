package ie.deed.ber.auth.store

import org.scalacheck.{Arbitrary, Gen}
import ie.deed.ber.auth.model.ApiKeyType
import ie.deed.ber.auth.model.ApiKey

import java.time.Instant
import java.time.temporal.{ChronoUnit, TemporalUnit}
import java.util.Date

val genApiKeyType = Gen.oneOf(ApiKeyType.values.toSeq)

val genApiKey: Gen[ApiKey] = for {
  email <- Gen.asciiStr
  apiKey <- Gen.asciiStr
  apiKeyType <- genApiKeyType
  createdAt <- Gen.choose(Instant.now().minus(20, ChronoUnit.DAYS), Instant.now())
} yield {
  ApiKey(
    email = email,
    apiKey = apiKey,
    apiKeyType = apiKeyType,
    createdAt = createdAt
  )
}

implicit val arbApiKey: Arbitrary[ApiKey] = Arbitrary(genApiKey)
