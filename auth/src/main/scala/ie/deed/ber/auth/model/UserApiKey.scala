package ie.deed.ber.auth.model

import java.time.Instant
import java.util.UUID

enum ApiKeyType:
  case Admin, Dev, User

/** apiKey is DocumentId
  */
case class UserApiKey(
    email: String,
    apiKey: String,
    tokenType: ApiKeyType,
    createdAt: Instant
)
