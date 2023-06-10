package ie.nok.ber.auth.model

import java.time.Instant
import java.util.UUID

enum ApiKeyType:
  case Admin, Dev, User

/** apiKey is DocumentId
  */
case class ApiKey(
    email: String,
    apiKey: String,
    apiKeyType: ApiKeyType,
    createdAt: Instant
)
